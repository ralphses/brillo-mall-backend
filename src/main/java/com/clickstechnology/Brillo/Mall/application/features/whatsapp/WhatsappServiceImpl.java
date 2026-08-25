package com.clickstechnology.Brillo.Mall.application.features.whatsapp;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ConversationService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.MessageSendService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.WhatsappService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationUpsertRequest;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageCreateRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.InteractiveMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.TemplateMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.TextMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsappResponse;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Body;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Button;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.ButtonAction;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Footer;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Section;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Template;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.MessageType;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.features.notifications.NotificationEventPublisher;
import com.clickstechnology.Brillo.Mall.application.features.runtime.ConversationTurnProcessor;
import com.clickstechnology.Brillo.Mall.application.features.runtime.TaskTurnResult;
import com.clickstechnology.Brillo.Mall.application.utils.WhatsappMessageGenerator;
import com.clickstechnology.Brillo.Mall.domain.conversation.MessageRepository;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
class WhatsappServiceImpl implements WhatsappService {

    private static final String TASK_MENU = "MENU";
    private static final String TASK_PRODUCT_PURCHASE = "PRODUCT_PURCHASE";
    private static final String TASK_SERVICE_DISCOVERY = "SERVICE_DISCOVERY";
    private static final String TASK_TRACKING = "TRACKING";
    private static final String TASK_BUSINESS_ONBOARDING = "BUSINESS_ONBOARDING";
    private static final String TASK_SUPPORT = "SUPPORT";
    private static final Pattern BRILLO_STORE_SLUG_PATTERN = Pattern.compile("\\bbrillo\\s+store\\s+([a-z0-9]+(?:-[a-z0-9]+)+)\\b");
    private static final Pattern STORE_SLUG_PATTERN = Pattern.compile("\\bstore\\s+([a-z0-9]+(?:-[a-z0-9]+)+)\\b");

    private final ConversationService conversationService;
    private final BusinessService businessService;
    private final CustomerService customerService;
    private final MessageSendService messageSendService;
    private final MessageRepository messageRepository;
    private final AppPropertiesConfig appPropertiesConfig;
    private final ObjectMapper objectMapper;
    private final ConversationTurnProcessor conversationTurnProcessor;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public String verifyWebhook(String mode, String verifyToken, String challenge) {
        if (!"subscribe".equalsIgnoreCase(mode) || verifyToken == null ||
                !verifyToken.equals(appPropertiesConfig.getWhatsapp().getVerifyToken())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid webhook verification request");
        }
        return challenge;
    }

    @Override
    public void handleWebhook(JsonNode payload) {
        processWebhookPayloadAsync(payload.deepCopy());
    }

    @Async
    void processWebhookPayloadAsync(JsonNode payload) {
        processWebhookPayload(payload);
    }

    @Override
    @Transactional
    public void processWebhookPayload(JsonNode payload) {
        for (WhatsappInboundEvent event : extractInboundEvents(payload)) {
            processInboundEvent(event);
        }
    }

    private void processInboundEvent(WhatsappInboundEvent event) {
        if (event.whatsappMessageId() == null || event.senderPhone() == null) {
            return;
        }

        if (messageRepository.existsByWhatsappMessageId(event.whatsappMessageId())) {
            return;
        }
        Optional<ConversationDto> existingConversation = conversationService.findByWhatsappId(event.whatsappConversationId());
        Instant now = event.occurredAt() != null ? event.occurredAt() : Instant.now();
        SharedEntryResolution sharedEntry = resolveSharedEntry(event, existingConversation.orElse(null));

        Optional<BusinessDto> dedicatedBusiness = resolveDedicatedBusiness(event);
        Optional<BusinessDto> conversationBusiness = existingConversation
                .flatMap(this::resolveConversationBusiness);
        Optional<BusinessDto> selectedBusiness = resolveBusinessSelection(event);
        BusinessDto business = sharedEntry.business()
                .or(() -> dedicatedBusiness)
                .or(() -> conversationBusiness)
                .or(() -> selectedBusiness)
                .orElse(null);

        if (business == null) {
            handleMarketplaceEntry(event, existingConversation.orElse(null), sharedEntry, now);
            return;
        }

        CustomerDto customer = customerService.resolveWhatsappCustomer(event.senderPhone(), event.senderName(), business.getId());
        ConversationDto baselineConversation = conversationService.upsertConversation(ConversationUpsertRequest.builder()
                .businessId(business.getId())
                .customerId(customer.getId())
                .whatsappConversationId(event.whatsappConversationId())
                .whatsappBusinessNumber(normalizePhoneNumber(event.businessPhoneNumber()))
                .entryBusinessId(resolveEntryBusinessId(existingConversation.orElse(null), sharedEntry, business))
                .activeBusinessId(business.getId())
                .entrySlug(resolveEntrySlug(existingConversation.orElse(null), sharedEntry))
                .marketplaceMode(false)
                .status(resolveConversationStatus(existingConversation.orElse(null), now))
                .lastIntent(existingConversation.map(ConversationDto::getLastIntent).orElse(null))
                .activeTaskKey(existingConversation.map(ConversationDto::getActiveTaskKey).orElse(null))
                .humanTakeover(existingConversation.map(ConversationDto::getHumanTakeover).orElse(Boolean.FALSE))
                .lastInteractionAt(now)
                .sessionExpiresAt(now.plus(appPropertiesConfig.getWhatsapp().getSessionWindowHours(), ChronoUnit.HOURS))
                .build());

        if (existingConversation.isPresent()
                && existingConversation.get().getSessionExpiresAt() != null
                && existingConversation.get().getSessionExpiresAt().isBefore(now)) {
            if (notificationEventPublisher != null) {
                notificationEventPublisher.publishSessionExpired(
                        recipientForBusiness(business),
                        business,
                        customer,
                        baselineConversation.getReference(),
                        "Conversation " + baselineConversation.getReference() + " expired and was reopened."
                );
            }
        }

        conversationService.addMessage(MessageCreateRequest.builder()
                .conversationReference(baselineConversation.getReference())
                .content(resolveInboundContent(event))
                .messageType(MessageType.INBOUND)
                .intent(existingConversation.map(ConversationDto::getLastIntent).orElse(null))
                .whatsappMessageId(event.whatsappMessageId())
                .transportType(event.inboundType())
                .sourceEventId(event.sourceEventId())
                .metadata(event.metadata())
                .build());

        if (Boolean.TRUE.equals(baselineConversation.getHumanTakeover())) {
            conversationService.upsertConversation(ConversationUpsertRequest.builder()
                    .businessId(business.getId())
                    .customerId(customer.getId())
                    .whatsappConversationId(event.whatsappConversationId())
                    .whatsappBusinessNumber(normalizePhoneNumber(event.businessPhoneNumber()))
                    .entryBusinessId(baselineConversation.getEntryBusinessId())
                    .activeBusinessId(business.getId())
                    .entrySlug(baselineConversation.getEntrySlug())
                    .marketplaceMode(false)
                    .status(ConversationStatus.HUMAN_TAKEOVER)
                    .lastIntent(baselineConversation.getLastIntent())
                    .activeTaskKey(baselineConversation.getActiveTaskKey())
                    .humanTakeover(true)
                    .lastInteractionAt(now)
                    .sessionExpiresAt(now.plus(appPropertiesConfig.getWhatsapp().getSessionWindowHours(), ChronoUnit.HOURS))
                    .build());
            return;
        }

        TaskTurnResult turnResult = conversationTurnProcessor.processTurn(baselineConversation, business, customer, event);
        if (turnResult.humanTakeover()) {
            if (notificationEventPublisher != null) {
                notificationEventPublisher.publishHumanTakeoverRequested(
                        recipientForBusiness(business),
                        business,
                        customer,
                        baselineConversation.getReference(),
                        "Human takeover requested for conversation " + baselineConversation.getReference() + "."
                );
            }
        }
        ConversationDto updatedConversation = conversationService.upsertConversation(ConversationUpsertRequest.builder()
                .businessId(business.getId())
                .customerId(customer.getId())
                .whatsappConversationId(event.whatsappConversationId())
                .whatsappBusinessNumber(normalizePhoneNumber(event.businessPhoneNumber()))
                .entryBusinessId(baselineConversation.getEntryBusinessId())
                .activeBusinessId(business.getId())
                .entrySlug(baselineConversation.getEntrySlug())
                .marketplaceMode(false)
                .status(turnResult.conversationStatus())
                .lastIntent(turnResult.intentKey())
                .activeTaskKey("SHOW_MENU".equals(turnResult.taskKey()) ? TASK_MENU : turnResult.taskKey())
                .humanTakeover(turnResult.humanTakeover())
                .lastInteractionAt(now)
                .sessionExpiresAt(now.plus(appPropertiesConfig.getWhatsapp().getSessionWindowHours(), ChronoUnit.HOURS))
                .build());

        if (turnResult.outboundMessage() != null) {
            WhatsappResponse response = messageSendService.sendMessage(turnResult.outboundMessage());
            String outboundMessageId = response != null && response.getMessages() != null && !response.getMessages().isEmpty()
                    ? response.getMessages().getFirst().getId()
                    : null;

            conversationService.addMessage(MessageCreateRequest.builder()
                    .conversationReference(updatedConversation.getReference())
                    .content(turnResult.replyText())
                    .messageType(MessageType.OUTBOUND)
                    .intent(turnResult.intentKey())
                    .whatsappMessageId(outboundMessageId)
                    .transportType(turnResult.presentationType() != null ? turnResult.presentationType().getValue() : WhatsappMessageType.TEXT.getValue())
                    .sourceEventId(event.whatsappMessageId())
                    .metadata(event.metadata())
                    .build());
        }
    }

    private ConversationStatus resolveConversationStatus(ConversationDto existingConversation, Instant now) {
        if (existingConversation == null) {
            return ConversationStatus.ACTIVE;
        }
        if (existingConversation.getSessionExpiresAt() != null && existingConversation.getSessionExpiresAt().isBefore(now)) {
            return ConversationStatus.ACTIVE;
        }
        return existingConversation.getStatus() != null ? existingConversation.getStatus() : ConversationStatus.ACTIVE;
    }

    private Optional<BusinessDto> resolveDedicatedBusiness(WhatsappInboundEvent event) {
        String businessNumber = normalizePhoneNumber(event.businessPhoneNumber());
        if (businessNumber == null) {
            return Optional.empty();
        }
        return businessService.findByWhatsappNumber(businessNumber);
    }

    private Optional<BusinessDto> resolveConversationBusiness(ConversationDto conversation) {
        String businessId = conversation.getActiveBusinessId() != null && !conversation.getActiveBusinessId().isBlank()
                ? conversation.getActiveBusinessId()
                : conversation.getBusinessId();
        if (businessId == null || businessId.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(businessService.findByBusinessId(businessId));
        } catch (BusinessException ex) {
            return Optional.empty();
        }
    }

    private Optional<BusinessDto> resolveBusinessSelection(WhatsappInboundEvent event) {
        String routeKey = event.interactiveReplyId() != null ? event.interactiveReplyId() : event.content();
        if (routeKey == null) {
            return Optional.empty();
        }
        String normalized = routeKey.trim();
        if (!normalized.startsWith("business:")) {
            return Optional.empty();
        }

        String businessRef = normalized.substring("business:".length()).trim();
        try {
            return Optional.of(businessService.findByBusinessId(businessRef));
        } catch (BusinessException ex) {
            return Optional.empty();
        }
    }

    private void handleMarketplaceEntry(
            WhatsappInboundEvent event,
            ConversationDto existingConversation,
            SharedEntryResolution sharedEntry,
            Instant now) {
        CustomerDto customer = customerService.resolveWhatsappCustomer(event.senderPhone(), event.senderName(), null);
        ConversationDto conversation = conversationService.upsertConversation(ConversationUpsertRequest.builder()
                .businessId(null)
                .customerId(customer.getId())
                .whatsappConversationId(event.whatsappConversationId())
                .whatsappBusinessNumber(normalizePhoneNumber(event.businessPhoneNumber()))
                .entryBusinessId(existingConversation != null ? existingConversation.getEntryBusinessId() : null)
                .activeBusinessId(null)
                .entrySlug(resolveEntrySlug(existingConversation, sharedEntry))
                .marketplaceMode(true)
                .status(ConversationStatus.AWAITING_USER)
                .lastIntent("MARKETPLACE_ENTRY")
                .activeTaskKey(TASK_MENU)
                .humanTakeover(existingConversation != null ? existingConversation.getHumanTakeover() : Boolean.FALSE)
                .lastInteractionAt(now)
                .sessionExpiresAt(now.plus(appPropertiesConfig.getWhatsapp().getSessionWindowHours(), ChronoUnit.HOURS))
                .build());

        conversationService.addMessage(MessageCreateRequest.builder()
                .conversationReference(conversation.getReference())
                .content(resolveInboundContent(event))
                .messageType(MessageType.INBOUND)
                .intent(conversation.getLastIntent())
                .whatsappMessageId(event.whatsappMessageId())
                .transportType(event.inboundType())
                .sourceEventId(event.sourceEventId())
                .metadata(event.metadata())
                .build());

        MarketplaceReplyPlan replyPlan = buildMarketplaceReplyPlan(event);
        WhatsappResponse response = messageSendService.sendMessage(replyPlan.outboundMessage());
        String outboundMessageId = response != null && response.getMessages() != null && !response.getMessages().isEmpty()
                ? response.getMessages().getFirst().getId()
                : null;

        conversationService.addMessage(MessageCreateRequest.builder()
                .conversationReference(conversation.getReference())
                .content(replyPlan.replyText())
                .messageType(MessageType.OUTBOUND)
                .intent("MARKETPLACE_ENTRY")
                .whatsappMessageId(outboundMessageId)
                .transportType(replyPlan.presentationType().getValue())
                .sourceEventId(event.whatsappMessageId())
                .metadata(event.metadata())
                .build());
    }

    private MarketplaceReplyPlan buildMarketplaceReplyPlan(WhatsappInboundEvent event) {
        List<BusinessDto> businesses = businessService.findWhatsappRouteCandidates();
        if (businesses.isEmpty()) {
            TextMessageRequest message = WhatsappMessageGenerator.createTextMessage(
                    event.senderPhone(),
                    "Welcome to Brillo Marketplace. No businesses are available on this WhatsApp route yet.",
                    false
            );
            return new MarketplaceReplyPlan(
                    "Welcome to Brillo Marketplace. No businesses are available on this WhatsApp route yet.",
                    WhatsappMessageType.TEXT,
                    message
            );
        }

        List<Section> sections = List.of(WhatsappMessageGenerator.createSection(
                businesses.stream()
                        .map(business -> WhatsappMessageGenerator.createRow(
                                business.getName(),
                                "Open " + business.getStorefrontName(),
                                "business:" + business.getId()))
                        .toList(),
                "Businesses"
        ));

        InteractiveMessageRequest request = WhatsappMessageGenerator.createListMessage(
                event.senderPhone(),
                WhatsappMessageGenerator.createTextHeader("Welcome to Brillo Marketplace"),
                Body.builder().text("Choose a store to continue, or tell me what you need.").build(),
                Footer.builder().text("Brillo shared marketplace").build(),
                "Browse stores",
                sections
        );
        return new MarketplaceReplyPlan(
                "Welcome to Brillo Marketplace. Choose a store to continue, or tell me what you need.",
                WhatsappMessageType.LIST,
                request
        );
    }

    private WhatsappReplyPlan planReply(WhatsappInboundEvent event, BusinessDto business, ConversationDto conversation, Instant now) {
        String incoming = normalizeText(resolveInboundContent(event));
        boolean expired = conversation.getSessionExpiresAt() != null && conversation.getSessionExpiresAt().isBefore(now);

        if (requestsHuman(event, incoming)) {
            TextMessageRequest request = WhatsappMessageGenerator.createTextMessage(
                    event.senderPhone(),
                    "A human agent has been notified and automation is paused for this conversation.",
                    false
            );
            return new WhatsappReplyPlan(
                    "A human agent has been notified and automation is paused for this conversation.",
                    WhatsappMessageType.TEXT,
                    request,
                    TASK_SUPPORT,
                    TASK_SUPPORT,
                    ConversationStatus.HUMAN_TAKEOVER,
                    true
            );
        }

        if (expired || isGreeting(incoming) || conversation.getActiveTaskKey() == null || TASK_MENU.equals(conversation.getActiveTaskKey())) {
            if (isMenuSelection(incoming, event.interactiveReplyId())) {
                return planSelectedJourney(event, conversation, incoming, business);
            }
            return welcomePlan(event, business);
        }

        if (isMenuSelection(incoming, event.interactiveReplyId())) {
            return planSelectedJourney(event, conversation, incoming, business);
        }

        if (conversation.getActiveTaskKey() != null) {
            return continueJourneyPlan(event, conversation);
        }

        return fallbackPlan(event);
    }

    private WhatsappReplyPlan welcomePlan(WhatsappInboundEvent event, BusinessDto business) {
        List<Section> sections = List.of(WhatsappMessageGenerator.createSection(List.of(
                WhatsappMessageGenerator.createRow("Buy something", "Products from " + business.getName(), "menu:buy"),
                WhatsappMessageGenerator.createRow("Find services", "Service requests and bookings", "menu:services"),
                WhatsappMessageGenerator.createRow("My orders & bookings", "Track an existing request", "menu:track"),
                WhatsappMessageGenerator.createRow("Onboard your business", "Start business setup", "menu:onboard"),
                WhatsappMessageGenerator.createRow("Support", "Talk to support or a human agent", "menu:support")
        ), "Main menu"));

        InteractiveMessageRequest request = WhatsappMessageGenerator.createListMessage(
                event.senderPhone(),
                WhatsappMessageGenerator.createTextHeader("Welcome to " + business.getName()),
                WhatsappMessageGenerator.createBody("What would you like to do today?"),
                WhatsappMessageGenerator.createFooter("Choose one option to continue"),
                "Open menu",
                sections
        );

        return new WhatsappReplyPlan(
                "What would you like to do today?",
                WhatsappMessageType.LIST,
                request,
                "GREETING",
                TASK_MENU,
                ConversationStatus.AWAITING_USER,
                false
        );
    }

    private WhatsappReplyPlan planSelectedJourney(WhatsappInboundEvent event, ConversationDto conversation, String incoming, BusinessDto business) {
        String selected = event.interactiveReplyId() != null ? event.interactiveReplyId() : incoming;
        String content;
        String taskKey;
        String intent;

        switch (selected) {
            case "menu:buy" -> {
                content = "Tell me the product you want to buy and I will keep the purchase session open for this business.";
                taskKey = TASK_PRODUCT_PURCHASE;
                intent = "PRODUCT_PURCHASE";
            }
            case "menu:services" -> {
                content = "Tell me the service you need, and I will keep the booking or negotiation flow open here.";
                taskKey = TASK_SERVICE_DISCOVERY;
                intent = "SERVICE_SEARCH";
            }
            case "menu:track" -> {
                content = "Send your order or booking reference and I will keep the tracking session active.";
                taskKey = TASK_TRACKING;
                intent = "ORDER_TRACKING";
            }
            case "menu:onboard" -> {
                String url = "https://brillo.example/onboard/" + business.getSlug();
                InteractiveMessageRequest request = WhatsappMessageGenerator.createCallToActionMessage(
                        event.senderPhone(),
                        WhatsappMessageGenerator.createBody("Use the secure onboarding link to continue business setup."),
                        WhatsappMessageGenerator.createFooter("You can return to WhatsApp any time."),
                        url,
                        "Continue",
                        null,
                        null,
                        null,
                        null
                );
                return new WhatsappReplyPlan(
                        "Use the secure onboarding link to continue business setup.",
                        WhatsappMessageType.CTA_URL,
                        request,
                        "BUSINESS_ONBOARDING",
                        TASK_BUSINESS_ONBOARDING,
                        ConversationStatus.AWAITING_USER,
                        false
                );
            }
            case "menu:support" -> {
                TextMessageRequest request = WhatsappMessageGenerator.createTextMessage(
                        event.senderPhone(),
                        "A support agent will continue from here shortly.",
                        false
                );
                return new WhatsappReplyPlan(
                        "A support agent will continue from here shortly.",
                        WhatsappMessageType.TEXT,
                        request,
                        TASK_SUPPORT,
                        TASK_SUPPORT,
                        ConversationStatus.HUMAN_TAKEOVER,
                        true
                );
            }
            default -> {
                return fallbackPlan(event);
            }
        }

        ButtonAction action = WhatsappMessageGenerator.createButtonAction(List.of(
                WhatsappMessageGenerator.createButton("menu:continue", "Continue"),
                WhatsappMessageGenerator.createButton("menu", "Main menu"),
                WhatsappMessageGenerator.createButton("menu:support", "Agent")
        ));

        InteractiveMessageRequest request = WhatsappMessageGenerator.createReplyButtonMessage(
                event.senderPhone(),
                WhatsappMessageGenerator.createTextHeader("Session started"),
                WhatsappMessageGenerator.createBody(content),
                WhatsappMessageGenerator.createFooter("Brillo will keep this session open for 24 hours."),
                action
        );

        return new WhatsappReplyPlan(
                content,
                WhatsappMessageType.BUTTON,
                request,
                intent,
                taskKey,
                ConversationStatus.AWAITING_USER,
                false
        );
    }

    private WhatsappReplyPlan continueJourneyPlan(WhatsappInboundEvent event, ConversationDto conversation) {
        String task = conversation.getActiveTaskKey();
        String content = switch (task) {
            case TASK_PRODUCT_PURCHASE -> "I am still on your product purchase request. Send the product details, quantity, or choose Main menu.";
            case TASK_SERVICE_DISCOVERY -> "I am still on your service request. Send the service details, preferred time, or choose Main menu.";
            case TASK_TRACKING -> "I am still on tracking. Send the order or booking reference you want checked.";
            case TASK_BUSINESS_ONBOARDING -> "I am still on onboarding. Use the last secure link or ask for support if you are stuck.";
            default -> "I am still on your current session. Choose Continue, Main menu, or Agent.";
        };

        ButtonAction action = WhatsappMessageGenerator.createButtonAction(List.of(
                WhatsappMessageGenerator.createButton("menu:continue", "Continue"),
                WhatsappMessageGenerator.createButton("menu", "Main menu"),
                WhatsappMessageGenerator.createButton("menu:support", "Agent")
        ));
        InteractiveMessageRequest request = WhatsappMessageGenerator.createReplyButtonMessage(
                event.senderPhone(),
                WhatsappMessageGenerator.createTextHeader("Session active"),
                WhatsappMessageGenerator.createBody(content),
                WhatsappMessageGenerator.createFooter("Structured fallback keeps the route safe."),
                action
        );

        return new WhatsappReplyPlan(
                content,
                WhatsappMessageType.BUTTON,
                request,
                conversation.getLastIntent(),
                conversation.getActiveTaskKey(),
                ConversationStatus.AWAITING_USER,
                false
        );
    }

    private WhatsappReplyPlan fallbackPlan(WhatsappInboundEvent event) {
        ButtonAction action = WhatsappMessageGenerator.createButtonAction(List.of(
                WhatsappMessageGenerator.createButton("menu:continue", "Continue"),
                WhatsappMessageGenerator.createButton("menu", "Main menu"),
                WhatsappMessageGenerator.createButton("menu:support", "Agent")
        ));
        InteractiveMessageRequest request = WhatsappMessageGenerator.createReplyButtonMessage(
                event.senderPhone(),
                WhatsappMessageGenerator.createTextHeader("Choose a safe route"),
                WhatsappMessageGenerator.createBody("I am not fully sure what you want yet. Choose Continue, Main menu, or Agent."),
                WhatsappMessageGenerator.createFooter("Brillo uses structured fallback when confidence is low."),
                action
        );

        return new WhatsappReplyPlan(
                "I am not fully sure what you want yet. Choose Continue, Main menu, or Agent.",
                WhatsappMessageType.BUTTON,
                request,
                "LOW_CONFIDENCE_FALLBACK",
                conversationTaskOrMenu(null),
                ConversationStatus.AWAITING_USER,
                false
        );
    }

    TemplateMessageRequest createTemplateMessage(String to, String templateName) {
        return TemplateMessageRequest.builder()
                .to(to)
                .template(Template.builder()
                        .name(templateName)
                        .language(Template.Language.builder().code("en").build())
                        .build())
                .build();
    }

    private boolean requestsHuman(WhatsappInboundEvent event, String incoming) {
        String replyId = event.interactiveReplyId();
        return "menu:support".equals(replyId) ||
                incoming.contains("human") ||
                incoming.contains("agent") ||
                incoming.contains("support");
    }

    private boolean isGreeting(String incoming) {
        return incoming.equals("hi") || incoming.equals("hello") || incoming.equals("hey") || incoming.equals("good morning");
    }

    private boolean isMenuSelection(String incoming, String replyId) {
        return (replyId != null && replyId.startsWith("menu:")) ||
                "menu".equals(incoming) ||
                incoming.startsWith("menu:");
    }

    private String conversationTaskOrMenu(String taskKey) {
        return taskKey != null ? taskKey : TASK_MENU;
    }

    private String resolveInboundContent(WhatsappInboundEvent event) {
        if (event.flowResponseJson() != null && !event.flowResponseJson().isBlank()) {
            return event.flowResponseJson();
        }
        if (event.interactiveReplyTitle() != null && !event.interactiveReplyTitle().isBlank()) {
            return event.interactiveReplyTitle();
        }
        if (event.content() != null && !event.content().isBlank()) {
            return event.content();
        }
        if (event.interactiveReplyId() != null) {
            return event.interactiveReplyId();
        }
        return "";
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private SharedEntryResolution resolveSharedEntry(WhatsappInboundEvent event, ConversationDto existingConversation) {
        if (event.interactiveReplyId() != null) {
            return new SharedEntryResolution(Optional.empty(), existingConversation != null ? existingConversation.getEntrySlug() : null);
        }

        if (existingConversation != null
                && ((existingConversation.getEntryBusinessId() != null && !existingConversation.getEntryBusinessId().isBlank())
                || (existingConversation.getActiveBusinessId() != null && !existingConversation.getActiveBusinessId().isBlank()))) {
            return new SharedEntryResolution(Optional.empty(), existingConversation.getEntrySlug());
        }

        String content = normalizeText(event.content());
        if (content.isBlank()) {
            return new SharedEntryResolution(Optional.empty(), null);
        }

        Set<String> slugCandidates = extractSlugCandidates(content);
        if (slugCandidates.size() != 1) {
            return new SharedEntryResolution(Optional.empty(), slugCandidates.size() == 1 ? slugCandidates.iterator().next() : null);
        }

        String slug = slugCandidates.iterator().next();
        return new SharedEntryResolution(businessService.findOptionalByBusinessSlug(slug), slug);
    }

    private Set<String> extractSlugCandidates(String content) {
        Set<String> candidates = new LinkedHashSet<>();
        collectSlugCandidates(BRILLO_STORE_SLUG_PATTERN.matcher(content), candidates);
        collectSlugCandidates(STORE_SLUG_PATTERN.matcher(content), candidates);
        return candidates;
    }

    private void collectSlugCandidates(Matcher matcher, Set<String> candidates) {
        while (matcher.find()) {
            candidates.add(matcher.group(1));
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return phoneNumber.replaceAll("[^\\d]", "");
    }

    private String recipientForBusiness(BusinessDto business) {
        return business.getWhatsappNumber() != null && !business.getWhatsappNumber().isBlank()
                ? business.getWhatsappNumber()
                : business.getPhoneNumber();
    }

    private String resolveEntryBusinessId(ConversationDto existingConversation, SharedEntryResolution sharedEntry, BusinessDto business) {
        if (existingConversation != null && existingConversation.getEntryBusinessId() != null && !existingConversation.getEntryBusinessId().isBlank()) {
            return existingConversation.getEntryBusinessId();
        }
        if (existingConversation != null && Boolean.TRUE.equals(existingConversation.getMarketplaceMode())) {
            return existingConversation.getEntryBusinessId();
        }
        if (sharedEntry.business().isPresent()) {
            return sharedEntry.business().get().getId();
        }
        return business.getId();
    }

    private String resolveEntrySlug(ConversationDto existingConversation, SharedEntryResolution sharedEntry) {
        if (existingConversation != null && existingConversation.getEntrySlug() != null && !existingConversation.getEntrySlug().isBlank()) {
            return existingConversation.getEntrySlug();
        }
        return sharedEntry.entrySlug();
    }

    private List<WhatsappInboundEvent> extractInboundEvents(JsonNode payload) {
        List<WhatsappInboundEvent> events = new ArrayList<>();
        JsonNode entries = payload.path("entry");
        if (!entries.isArray()) {
            return events;
        }

        for (JsonNode entry : entries) {
            for (JsonNode change : entry.path("changes")) {
                JsonNode value = change.path("value");
                JsonNode metadata = value.path("metadata");
                JsonNode messages = value.path("messages");
                JsonNode contacts = value.path("contacts");
                if (!messages.isArray()) {
                    continue;
                }

                for (JsonNode message : messages) {
                    String from = normalizePhoneNumber(message.path("from").asText(null));
                    String profileName = resolveProfileName(contacts, from);
                    String type = message.path("type").asText("text");
                    String textBody = message.path("text").path("body").asText(null);
                    String interactiveReplyId = resolveInteractiveReplyId(message);
                    String interactiveReplyTitle = resolveInteractiveReplyTitle(message);
                    String flowId = resolveFlowId(message);
                    String flowName = resolveFlowName(message);
                    String flowToken = resolveFlowToken(message);
                    String flowResponseJson = resolveFlowResponseJson(message);
                    Instant occurredAt = resolveTimestamp(message.path("timestamp").asText(null));

                    events.add(new WhatsappInboundEvent(
                            from,
                            profileName,
                            metadata.path("display_phone_number").asText(null),
                            metadata.path("phone_number_id").asText(null),
                            message.path("id").asText(null),
                            from,
                            message.path("id").asText(null),
                            textBody,
                            interactiveReplyId,
                            interactiveReplyTitle,
                            flowId,
                            flowName,
                            flowToken,
                            flowResponseJson,
                            type,
                            message.path("context").path("id").asText(null),
                            occurredAt,
                            stringify(message)
                    ));
                }
            }
        }
        return events;
    }

    private String resolveProfileName(JsonNode contacts, String from) {
        if (!contacts.isArray()) {
            return from;
        }
        for (JsonNode contact : contacts) {
            if (normalizePhoneNumber(contact.path("wa_id").asText(null)).equals(from)) {
                return contact.path("profile").path("name").asText(from);
            }
        }
        return from;
    }

    private String resolveInteractiveReplyId(JsonNode message) {
        JsonNode interactive = message.path("interactive");
        if (interactive.isMissingNode()) {
            return null;
        }
        if (interactive.has("button_reply")) {
            return interactive.path("button_reply").path("id").asText(null);
        }
        if (interactive.has("list_reply")) {
            return interactive.path("list_reply").path("id").asText(null);
        }
        return null;
    }

    private String resolveInteractiveReplyTitle(JsonNode message) {
        JsonNode interactive = message.path("interactive");
        if (interactive.isMissingNode()) {
            return null;
        }
        if (interactive.has("button_reply")) {
            return interactive.path("button_reply").path("title").asText(null);
        }
        if (interactive.has("list_reply")) {
            return interactive.path("list_reply").path("title").asText(null);
        }
        return null;
    }

    private String resolveFlowId(JsonNode message) {
        JsonNode interactive = message.path("interactive");
        if (interactive.isMissingNode()) {
            return null;
        }
        JsonNode flowReply = interactive.path("nfm_reply");
        if (!flowReply.isMissingNode()) {
            return flowReply.path("flow_id").asText(null);
        }
        return interactive.path("flow").path("id").asText(null);
    }

    private String resolveFlowName(JsonNode message) {
        JsonNode interactive = message.path("interactive");
        if (interactive.isMissingNode()) {
            return null;
        }
        JsonNode flowReply = interactive.path("nfm_reply");
        if (!flowReply.isMissingNode()) {
            return flowReply.path("name").asText(null);
        }
        return interactive.path("flow").path("name").asText(null);
    }

    private String resolveFlowToken(JsonNode message) {
        JsonNode interactive = message.path("interactive");
        if (interactive.isMissingNode()) {
            return null;
        }
        JsonNode flowReply = interactive.path("nfm_reply");
        if (!flowReply.isMissingNode()) {
            return flowReply.path("flow_token").asText(null);
        }
        return interactive.path("flow").path("flow_token").asText(null);
    }

    private String resolveFlowResponseJson(JsonNode message) {
        JsonNode interactive = message.path("interactive");
        if (interactive.isMissingNode()) {
            return null;
        }
        JsonNode flowReply = interactive.path("nfm_reply");
        if (!flowReply.isMissingNode()) {
            String responseJson = flowReply.path("response_json").asText(null);
            if (responseJson != null && !responseJson.isBlank()) {
                return responseJson;
            }
        }
        JsonNode flowResponse = interactive.path("flow_response");
        if (!flowResponse.isMissingNode()) {
            String responseJson = flowResponse.path("response_json").asText(null);
            if (responseJson != null && !responseJson.isBlank()) {
                return responseJson;
            }
        }
        return null;
    }

    private Instant resolveTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.ofEpochSecond(Long.parseLong(timestamp));
        } catch (NumberFormatException ex) {
            return Instant.now();
        }
    }

    private String stringify(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            log.debug("Unable to serialize WhatsApp message metadata", ex);
            return null;
        }
    }

    private record SharedEntryResolution(Optional<BusinessDto> business, String entrySlug) { }

    private record MarketplaceReplyPlan(
            String replyText,
            WhatsappMessageType presentationType,
            WhatsAppMessageRequest outboundMessage
    ) { }
}
