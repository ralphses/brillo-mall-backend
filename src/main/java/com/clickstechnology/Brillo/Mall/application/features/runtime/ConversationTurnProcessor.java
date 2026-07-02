package com.clickstechnology.Brillo.Mall.application.features.runtime;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.InteractiveMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.TextMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Body;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.ButtonAction;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Footer;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Header;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Section;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.TaskDecisionSource;
import com.clickstechnology.Brillo.Mall.application.enums.TaskSessionStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.utils.WhatsappMessageGenerator;
import com.clickstechnology.Brillo.Mall.application.features.whatsapp.WhatsappInboundEvent;
import com.clickstechnology.Brillo.Mall.domain.conversation.task.ConversationTaskSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationTurnProcessor {

    private static final double MIN_AI_CONFIDENCE = 0.75d;
    private static final int DEFAULT_PAGE_SIZE = 5;

    private final TaskCatalogLoader catalogLoader;
    private final TaskSessionService taskSessionService;
    private final ChatGptAiClient aiClient;
    private final ProductService productService;
    private final BusinessServiceService businessServiceService;
    private final OrderService orderService;

    @Transactional
    public TaskTurnResult processTurn(
            ConversationDto conversation,
            BusinessDto business,
            CustomerDto customer,
            WhatsappInboundEvent event
    ) {
        String to = event.senderPhone();
        String normalizedInput = catalogLoader.normalize(resolveInput(event));
        ConversationTaskSession session = taskSessionService.getOrCreate(conversation, business.getId(), customer.getId());
        Map<String, Object> slots = new LinkedHashMap<>(taskSessionService.readSlots(session));

        if (session.getHumanTakeover() != null && session.getHumanTakeover()) {
            return buildHumanTakeoverResult(session, conversation, normalizedInput, event, slots, to);
        }

        RouteResolution routeResolution = resolveRoute(event, normalizedInput, conversation, session);
        String routeTo = routeResolution.routeTo();
        String taskKey = resolveTaskKey(routeTo, session);
        if (taskKey == null || taskKey.isBlank()) {
            return buildFallbackResult(session, conversation, normalizedInput, event, slots, routeResolution.reason(), to);
        }

        String stateKey = resolveState(session, taskKey);
        List<String> requiredSlots = new ArrayList<>(catalogLoader.requiredSlots(taskKey, stateKey));
        List<String> optionalSlots = new ArrayList<>(catalogLoader.optionalSlots(taskKey, stateKey));
        Set<String> allowedSlots = new LinkedHashSet<>();
        allowedSlots.addAll(requiredSlots);
        allowedSlots.addAll(optionalSlots);
        allowedSlots.addAll(catalogLoader.globalSlots(taskKey));

        Map<String, Object> extractedSlots = extractSlots(normalizedInput, taskKey, stateKey, allowedSlots, conversation, business, customer, session, slots, event);
        mergeSlots(slots, extractedSlots);

        List<String> missingSlots = requiredSlots.stream()
                .filter(slot -> isEmptyValue(slots.get(slot)))
                .toList();

        String resolvedState = stateKey;
        TaskSessionStatus taskSessionStatus = TaskSessionStatus.ACTIVE;
        ConversationStatus conversationStatus = ConversationStatus.AWAITING_USER;
        boolean humanTakeover = Boolean.TRUE.equals(session.getHumanTakeover());
        String replyText;
        WhatsappMessageType presentationType;
        WhatsAppMessageRequest outboundMessage;
        String aiReason = routeResolution.reason();
        String aiModel = routeResolution.model();
        double confidence = routeResolution.confidence();
        TaskDecisionSource decisionSource = routeResolution.source();

        if (!missingSlots.isEmpty()) {
            taskSessionStatus = TaskSessionStatus.PAUSED;
            session.setPausedTaskKey(taskKey);
            session.setPausedStateKey(stateKey);
            replyText = buildSlotPrompt(taskKey, stateKey, missingSlots, normalizedInput);
            presentationType = WhatsappMessageType.BUTTON;
            outboundMessage = buildFallbackButtons(to, replyText);
        } else {
            StateAdvance stateAdvance = advanceState(taskKey, stateKey, slots, to, business, customer);
            resolvedState = stateAdvance.nextState();
            replyText = buildTurnReply(taskKey, resolvedState, business, customer, slots, event);
            presentationType = stateAdvance.presentationType();
            outboundMessage = stateAdvance.outboundMessage();
            taskSessionStatus = stateAdvance.taskSessionStatus();
            conversationStatus = stateAdvance.conversationStatus();
            humanTakeover = stateAdvance.humanTakeover();
            confidence = Math.max(confidence, stateAdvance.confidence());
            if (stateAdvance.reason() != null && !stateAdvance.reason().isBlank()) {
                aiReason = stateAdvance.reason();
            }
            if (stateAdvance.model() != null && !stateAdvance.model().isBlank()) {
                aiModel = stateAdvance.model();
            }
            decisionSource = stateAdvance.decisionSource();
        }

        if (session.getTaskStatus() == TaskSessionStatus.PAUSED && "continue".equals(normalizedInput)) {
            taskSessionStatus = TaskSessionStatus.ACTIVE;
            conversationStatus = ConversationStatus.AWAITING_USER;
        }

        if (routeResolution.taskStatus() == TaskSessionStatus.ESCALATED) {
            taskSessionStatus = TaskSessionStatus.ESCALATED;
            conversationStatus = ConversationStatus.HUMAN_TAKEOVER;
            humanTakeover = true;
            session.setPausedTaskKey(taskKey);
            session.setPausedStateKey(stateKey);
            replyText = "A human agent will continue from here shortly.";
            presentationType = WhatsappMessageType.TEXT;
            outboundMessage = WhatsappMessageGenerator.createTextMessage(to, replyText, false);
        }

        if (routeResolution.taskStatus() == TaskSessionStatus.FALLBACK) {
            taskSessionStatus = TaskSessionStatus.FALLBACK;
            conversationStatus = ConversationStatus.AWAITING_USER;
            replyText = "I am not fully sure what you want yet. Choose a safe route below.";
            presentationType = WhatsappMessageType.BUTTON;
            outboundMessage = buildFallbackButtons(to, replyText);
        }

        if (isTerminalState(taskKey, resolvedState)) {
            taskSessionStatus = TaskSessionStatus.COMPLETED;
            conversationStatus = ConversationStatus.ACTIVE;
            session.setPausedTaskKey(null);
            session.setPausedStateKey(null);
        }

        TaskTurnResult result = new TaskTurnResult(
                routeResolution.intentKey(),
                routeTo,
                taskKey,
                resolvedState,
                confidence,
                decisionSource,
                taskSessionStatus,
                conversationStatus,
                humanTakeover,
                replyText,
                presentationType,
                outboundMessage,
                slots,
                missingSlots,
                aiReason,
                aiModel
        );

        taskSessionService.persistTransition(session, result, resolveInput(event), normalizedInput, slots, event.metadata());
        return result;
    }

    private RouteResolution resolveRoute(
            WhatsappInboundEvent event,
            String normalizedInput,
            ConversationDto conversation,
            ConversationTaskSession session
    ) {
        String interactiveId = Optional.ofNullable(event.interactiveReplyId()).orElse("");
        String selectedText = normalizedInput;

        if (event.interactiveReplyId() != null || normalizedInput.startsWith("menu:") || normalizedInput.equals("menu")) {
            return resolveMenuRoute(interactiveId.isBlank() ? normalizedInput : interactiveId, session, conversation);
        }

        if (requestsHuman(event, normalizedInput)) {
            return new RouteResolution("SUPPORT_REQUEST", "SUPPORT_TASK", TaskDecisionSource.RULE, 1.0d, false, "Human support requested", null, TaskSessionStatus.ESCALATED);
        }

        if (isGreeting(normalizedInput)) {
            return new RouteResolution("GREETING", "SHOW_MENU", TaskDecisionSource.RULE, 1.0d, false, "Greeting detected", null, TaskSessionStatus.ACTIVE);
        }

        List<RouteCandidate> candidates = findRuleCandidates(normalizedInput, selectedText);
        if (candidates.size() == 1) {
            RouteCandidate candidate = candidates.getFirst();
            return new RouteResolution(candidate.intentKey(), candidate.routeTo(), TaskDecisionSource.RULE, 0.99d, false, "Rule match", null, TaskSessionStatus.ACTIVE);
        }

        if (candidates.size() > 1) {
            Set<String> candidateIntents = candidates.stream().map(RouteCandidate::intentKey).collect(Collectors.toCollection(LinkedHashSet::new));
            Optional<AiAmbiguityDecision> ambiguityDecision = aiClient.detectAmbiguity(normalizedInput, candidateIntents, buildContext(conversation, session));
            if (ambiguityDecision.isPresent()) {
                AiAmbiguityDecision decision = ambiguityDecision.get();
                if (decision.ambiguous() || decision.confidence() < MIN_AI_CONFIDENCE) {
                    return new RouteResolution(
                            decision.preferredIntentKey(),
                            Optional.ofNullable(decision.preferredRouteTo()).orElse("SHOW_MENU"),
                            TaskDecisionSource.FALLBACK,
                            decision.confidence(),
                            true,
                            decision.reason(),
                            decision.model(),
                            TaskSessionStatus.FALLBACK
                    );
                }
                return new RouteResolution(
                        Optional.ofNullable(decision.preferredIntentKey()).orElse(candidates.getFirst().intentKey()),
                        Optional.ofNullable(decision.preferredRouteTo()).orElse(candidates.getFirst().routeTo()),
                        TaskDecisionSource.AI,
                        decision.confidence(),
                        false,
                        decision.reason(),
                        decision.model(),
                        TaskSessionStatus.ACTIVE
                );
            }
        }

        if (aiClient.isEnabled()) {
            Optional<AiIntentDecision> aiDecision = aiClient.classifyIntent(normalizedInput, catalogLoader.knownIntentKeys(), buildContext(conversation, session));
            if (aiDecision.isPresent()) {
                AiIntentDecision decision = aiDecision.get();
                if (decision.ambiguous() || decision.confidence() < MIN_AI_CONFIDENCE || decision.intentKey() == null) {
                    return new RouteResolution(
                            decision.intentKey(),
                            Optional.ofNullable(decision.routeTo()).orElse("SHOW_MENU"),
                            TaskDecisionSource.FALLBACK,
                            decision.confidence(),
                            true,
                            decision.reason(),
                            decision.model(),
                            TaskSessionStatus.FALLBACK
                    );
                }
                return new RouteResolution(
                        decision.intentKey(),
                        Optional.ofNullable(decision.routeTo()).orElseGet(() -> catalogLoader.routeTo(decision.intentKey()).orElse("SHOW_MENU")),
                        TaskDecisionSource.AI,
                        decision.confidence(),
                        false,
                        decision.reason(),
                        decision.model(),
                        TaskSessionStatus.ACTIVE
                );
            }
        }

        return new RouteResolution(null, "SHOW_MENU", TaskDecisionSource.FALLBACK, 0.0d, true, "No reliable route", null, TaskSessionStatus.FALLBACK);
    }

    private RouteResolution resolveMenuRoute(String routeKey, ConversationTaskSession session, ConversationDto conversation) {
        String normalizedRoute = routeKey.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedRoute) {
            case "menu", "show_menu" -> new RouteResolution("GREETING", "SHOW_MENU", TaskDecisionSource.RULE, 1.0d, false, "Menu requested", null, TaskSessionStatus.ACTIVE);
            case "menu:buy", "buy", "product_purchase" -> new RouteResolution("PRODUCT_PURCHASE", "PRODUCT_PURCHASE_TASK", TaskDecisionSource.RULE, 1.0d, false, "Buy flow selected", null, TaskSessionStatus.ACTIVE);
            case "menu:services", "services", "service_search" -> new RouteResolution("SERVICE_SEARCH", "SERVICE_SEARCH_TASK", TaskDecisionSource.RULE, 1.0d, false, "Service flow selected", null, TaskSessionStatus.ACTIVE);
            case "menu:track", "track", "tracking" -> new RouteResolution("ORDER_TRACKING", "ORDER_TRACKING_TASK", TaskDecisionSource.RULE, 1.0d, false, "Tracking selected", null, TaskSessionStatus.ACTIVE);
            case "menu:onboard", "onboard", "business_onboarding" -> new RouteResolution("BUSINESS_ONBOARDING", "BUSINESS_ONBOARDING_TASK", TaskDecisionSource.RULE, 1.0d, false, "Onboarding selected", null, TaskSessionStatus.ACTIVE);
            case "menu:support", "support" -> new RouteResolution("SUPPORT_REQUEST", "SUPPORT_TASK", TaskDecisionSource.RULE, 1.0d, false, "Support requested", null, TaskSessionStatus.ESCALATED);
            case "menu:continue", "continue" -> new RouteResolution(
                    Optional.ofNullable(session.getCurrentIntent()).orElse(conversation.getLastIntent()),
                    Optional.ofNullable(session.getCurrentTaskKey()).orElse(conversation.getActiveTaskKey()),
                    TaskDecisionSource.RULE,
                    1.0d,
                    false,
                    "Continue requested",
                    null,
                    TaskSessionStatus.ACTIVE
            );
            default -> new RouteResolution(null, "SHOW_MENU", TaskDecisionSource.FALLBACK, 0.0d, true, "Unknown menu selection", null, TaskSessionStatus.FALLBACK);
        };
    }

    private List<RouteCandidate> findRuleCandidates(String normalizedInput, String selectedText) {
        Set<RouteCandidate> matches = new LinkedHashSet<>();
        for (String intentKey : catalogLoader.knownIntentKeys()) {
            List<String> examples = catalogLoader.intentExamples(intentKey);
            for (String example : examples) {
                String normalizedExample = catalogLoader.normalize(example);
                if (!normalizedExample.isBlank() && normalizedInput.equals(normalizedExample)) {
                    matches.add(new RouteCandidate(intentKey, catalogLoader.routeTo(intentKey).orElse("SHOW_MENU")));
                }
            }
        }
        if (!matches.isEmpty()) {
            return new ArrayList<>(matches);
        }

        Map<String, RouteCandidate> keywords = new LinkedHashMap<>();
        keywords.put("buy", new RouteCandidate("PRODUCT_PURCHASE", "PRODUCT_PURCHASE_TASK"));
        keywords.put("purchase", new RouteCandidate("PRODUCT_PURCHASE", "PRODUCT_PURCHASE_TASK"));
        keywords.put("order", new RouteCandidate("ORDER_TRACKING", "ORDER_TRACKING_TASK"));
        keywords.put("track", new RouteCandidate("ORDER_TRACKING", "ORDER_TRACKING_TASK"));
        keywords.put("service", new RouteCandidate("SERVICE_SEARCH", "SERVICE_SEARCH_TASK"));
        keywords.put("book", new RouteCandidate("SERVICE_BOOKING", "SERVICE_REQUEST_TASK"));
        keywords.put("negotiate", new RouteCandidate("NEGOTIATION_REQUEST", "NEGOTIATION_TASK"));
        keywords.put("discount", new RouteCandidate("NEGOTIATION_REQUEST", "NEGOTIATION_TASK"));
        keywords.put("pay", new RouteCandidate("PAYMENT_INITIATION", "PAYMENT_TASK"));
        keywords.put("support", new RouteCandidate("SUPPORT_REQUEST", "SUPPORT_TASK"));
        keywords.put("help", new RouteCandidate("SUPPORT_REQUEST", "SUPPORT_TASK"));
        keywords.put("onboard", new RouteCandidate("BUSINESS_ONBOARDING", "BUSINESS_ONBOARDING_TASK"));

        for (Map.Entry<String, RouteCandidate> entry : keywords.entrySet()) {
            if (normalizedInput.contains(entry.getKey())) {
                matches.add(entry.getValue());
            }
        }
        return new ArrayList<>(matches);
    }

    private Map<String, Object> buildContext(ConversationDto conversation, ConversationTaskSession session) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("conversationReference", conversation.getReference());
        context.put("businessId", conversation.getBusinessId());
        context.put("customerId", conversation.getCustomerId());
        context.put("currentTaskKey", session.getCurrentTaskKey());
        context.put("currentStateKey", session.getCurrentStateKey());
        context.put("taskStatus", session.getTaskStatus() != null ? session.getTaskStatus().name() : null);
        context.put("lastIntent", conversation.getLastIntent());
        context.put("humanTakeover", conversation.getHumanTakeover());
        return context;
    }

    private String resolveTaskKey(String routeTo, ConversationTaskSession session) {
        if (routeTo == null || routeTo.isBlank()) {
            return Optional.ofNullable(session.getCurrentTaskKey()).orElse("SHOW_MENU");
        }

        return switch (routeTo) {
            case "SHOW_MENU" -> "SHOW_MENU";
            case "USER_ONBOARDING_TASK", "BUSINESS_ONBOARDING_TASK", "PRODUCT_SEARCH_TASK", "PRODUCT_PURCHASE_TASK",
                    "ORDER_TRACKING_TASK", "ORDER_MODIFICATION_TASK", "SERVICE_SEARCH_TASK", "SERVICE_REQUEST_TASK",
                    "SERVICE_TRACKING_TASK", "NEGOTIATION_TASK", "PAYMENT_TASK", "BUSINESS_PRODUCT_ORDER_TASK",
                    "BUSINESS_SERVICE_REQUEST_TASK", "SUPPORT_TASK", "GLOBAL_SEARCH_TASK", "ORDER_STATUS_QUERY_TASK",
                    "AUTH_GUARD_TASK", "SESSION_ROUTING_TASK" -> routeTo;
            default -> {
                String mapped = catalogLoader.routeTo(routeTo).orElse(routeTo);
                yield mapped;
            }
        };
    }

    private String resolveState(ConversationTaskSession session, String taskKey) {
        if (taskKey == null || taskKey.isBlank() || "SHOW_MENU".equals(taskKey)) {
            return "SHOW_MENU";
        }

        if (!taskKey.equals(session.getCurrentTaskKey()) || session.getCurrentStateKey() == null || session.getCurrentStateKey().isBlank()) {
            return firstState(taskKey);
        }

        return session.getCurrentStateKey();
    }

    private String firstState(String taskKey) {
        List<String> states = catalogLoader.taskStates(taskKey);
        return states.isEmpty() ? "COMPLETED" : states.getFirst();
    }

    private Map<String, Object> extractSlots(
            String normalizedInput,
            String taskKey,
            String stateKey,
            Set<String> allowedSlots,
            ConversationDto conversation,
            BusinessDto business,
            CustomerDto customer,
            ConversationTaskSession session,
            Map<String, Object> currentSlots,
            WhatsappInboundEvent event
    ) {
        Map<String, Object> extracted = new LinkedHashMap<>(deterministicSlotExtraction(normalizedInput, allowedSlots, conversation, business, customer, session, currentSlots));
        if (aiClient.isEnabled()) {
            Optional<AiSlotDecision> aiDecision = aiClient.extractSlots(normalizedInput, taskKey, stateKey, allowedSlots, buildSlotContext(conversation, business, customer, session, currentSlots));
            if (aiDecision.isPresent() && aiDecision.get().confidence() >= MIN_AI_CONFIDENCE) {
                mergeSlots(extracted, aiDecision.get().slots(), allowedSlots);
            }
        }
        return extracted;
    }

    private Map<String, Object> deterministicSlotExtraction(
            String normalizedInput,
            Set<String> allowedSlots,
            ConversationDto conversation,
            BusinessDto business,
            CustomerDto customer,
            ConversationTaskSession session,
            Map<String, Object> currentSlots
    ) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String slot : allowedSlots) {
            switch (slot) {
                case "query" -> values.put("query", normalizedInput);
                case "product_id" -> findReference(normalizedInput, "product").ifPresent(value -> values.put("product_id", value));
                case "service_id" -> findReference(normalizedInput, "service").ifPresent(value -> values.put("service_id", value));
                case "order_id" -> findReference(normalizedInput, "order").ifPresent(value -> values.put("order_id", value));
                case "request_id" -> findReference(normalizedInput, "request").ifPresent(value -> values.put("request_id", value));
                case "selected_order_id" -> findReference(normalizedInput, "order").ifPresent(value -> values.put("selected_order_id", value));
                case "selected_request_id" -> findReference(normalizedInput, "request").ifPresent(value -> values.put("selected_request_id", value));
                case "payment_method" -> extractPaymentMethod(normalizedInput).ifPresent(value -> values.put("payment_method", value));
                case "quantity" -> extractQuantity(normalizedInput).ifPresent(value -> values.put("quantity", value));
                case "location" -> extractLocation(normalizedInput).ifPresent(value -> values.put("location", value));
                case "message" -> values.put("message", normalizedInput);
                case "user_id" -> {
                    if (customer.getUserId() != null) {
                        values.put("user_id", customer.getUserId());
                    }
                }
                case "business_name" -> values.put("business_name", business.getName());
                case "business_type" -> values.put("business_type", business.getCategory() != null ? business.getCategory().name() : null);
                case "customer_id" -> values.put("customer_id", customer.getId());
                default -> {
                    if (currentSlots.containsKey(slot) && currentSlots.get(slot) != null) {
                        values.putIfAbsent(slot, currentSlots.get(slot));
                    }
                }
            }
        }
        return values;
    }

    private Map<String, Object> buildSlotContext(ConversationDto conversation, BusinessDto business, CustomerDto customer, ConversationTaskSession session, Map<String, Object> currentSlots) {
        Map<String, Object> context = buildContext(conversation, session);
        context.put("businessName", business.getName());
        context.put("businessSlug", business.getSlug());
        context.put("customerName", customer.getCustomerName());
        context.put("currentSlots", currentSlots);
        return context;
    }

    private Optional<String> findReference(String input, String prefix) {
        String token = prefix + ":";
        int index = input.indexOf(token);
        if (index < 0) {
            return Optional.empty();
        }
        String raw = input.substring(index + token.length()).trim();
        String reference = raw.split("\\s+")[0];
        return reference.isBlank() ? Optional.empty() : Optional.of(reference);
    }

    private Optional<Integer> extractQuantity(String input) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(input);
        if (matcher.find()) {
            try {
                return Optional.of(Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractPaymentMethod(String input) {
        if (input.contains("cash")) {
            return Optional.of("CASH");
        }
        if (input.contains("transfer")) {
            return Optional.of("TRANSFER");
        }
        if (input.contains("card")) {
            return Optional.of("CARD");
        }
        if (input.contains("paystack")) {
            return Optional.of("PAYSTACK");
        }
        return Optional.empty();
    }

    private Optional<String> extractLocation(String input) {
        if (input.contains("address")) {
            return Optional.of(input);
        }
        if (input.contains("at ")) {
            return Optional.of(input.substring(input.indexOf("at ") + 3).trim());
        }
        return Optional.empty();
    }

    private void mergeSlots(Map<String, Object> target, Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return;
        }
        source.forEach((key, value) -> {
            if (value != null && !isEmptyValue(value)) {
                target.put(key, value);
            }
        });
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String string) {
            return string.isBlank();
        }
        if (value instanceof List<?> list) {
            return list.isEmpty();
        }
        return false;
    }

    private StateAdvance advanceState(String taskKey, String stateKey, Map<String, Object> slots, String to, BusinessDto business, CustomerDto customer) {
        List<String> states = catalogLoader.taskStates(taskKey);
        if (states.isEmpty()) {
            ReplyPlan plan = genericTextReply(to, "I am ready for the next step.");
            return new StateAdvance(stateKey, plan.replyText(), plan.type(), TaskSessionStatus.ACTIVE, ConversationStatus.AWAITING_USER, false, TaskDecisionSource.RULE, 0.9d, plan.message(), null, null);
        }

        int index = states.indexOf(stateKey);
        if (index < 0) {
            index = 0;
        }

        List<String> required = catalogLoader.requiredSlots(taskKey, stateKey);
        boolean complete = required.stream().allMatch(slot -> !isEmptyValue(slots.get(slot)));
        String nextState = stateKey;
        if (complete && index < states.size() - 1) {
            nextState = states.get(index + 1);
        }

        if (isTerminalState(taskKey, nextState)) {
            ReplyPlan completion = buildCompletionReply(to, taskKey);
            return new StateAdvance(nextState, completion.replyText(), completion.type(), TaskSessionStatus.COMPLETED, ConversationStatus.ACTIVE, false, TaskDecisionSource.RULE, 0.95d, completion.message(), null, null);
        }

        ReplyPlan replyPlan = buildTaskReply(taskKey, nextState, slots, to, business, customer);
        return new StateAdvance(nextState, replyPlan.replyText(), replyPlan.type(), replyPlan.sessionStatus(), replyPlan.conversationStatus(), replyPlan.humanTakeover(), replyPlan.decisionSource(), replyPlan.confidence(), replyPlan.message(), replyPlan.reason(), null);
    }

    private String buildTurnReply(String taskKey, String stateKey, BusinessDto business, CustomerDto customer, Map<String, Object> slots, WhatsappInboundEvent event) {
        return switch (taskKey) {
            case "SHOW_MENU" -> "What would you like to do today?";
            case "PRODUCT_SEARCH_TASK" -> "I am searching products for you.";
            case "SERVICE_SEARCH_TASK" -> "I am searching services for you.";
            case "ORDER_TRACKING_TASK", "ORDER_STATUS_QUERY_TASK" -> "I am checking the status for you.";
            case "SERVICE_REQUEST_TASK" -> "I am preparing your service request.";
            case "NEGOTIATION_TASK" -> "I am keeping the negotiation open.";
            case "PAYMENT_TASK" -> "I am preparing the payment step.";
            case "SUPPORT_TASK" -> "A support agent will continue from here shortly.";
            case "BUSINESS_ONBOARDING_TASK" -> "Use the secure onboarding flow to continue.";
            case "USER_ONBOARDING_TASK" -> "Let us finish your account setup.";
            case "BUSINESS_PRODUCT_ORDER_TASK" -> "I am loading your business orders.";
            case "BUSINESS_SERVICE_REQUEST_TASK" -> "I am loading your business service requests.";
            case "GLOBAL_SEARCH_TASK" -> "I am searching across your available records.";
            default -> "I am still on your current session.";
        };
    }

    private ReplyPlan buildTaskReply(String taskKey, String stateKey, Map<String, Object> slots, String to, BusinessDto business, CustomerDto customer) {
        return switch (taskKey) {
            case "SHOW_MENU" -> menuReply(to);
            case "PRODUCT_SEARCH_TASK" -> searchProductsReply(to, business.getId(), slots);
            case "SERVICE_SEARCH_TASK" -> searchServicesReply(to, business.getId(), slots);
            case "ORDER_TRACKING_TASK", "ORDER_STATUS_QUERY_TASK" -> orderTrackingReply(to, customer.getId(), slots);
            case "SERVICE_REQUEST_TASK" -> serviceRequestReply(to, business.getId(), slots);
            case "NEGOTIATION_TASK" -> negotiationReply(to, slots);
            case "PAYMENT_TASK" -> paymentReply(to, slots);
            case "SUPPORT_TASK" -> supportReply(to);
            case "BUSINESS_ONBOARDING_TASK" -> onboardingReply(to, "Use the secure onboarding link to continue business setup.");
            case "USER_ONBOARDING_TASK" -> onboardingReply(to, "Let us finish your account setup.");
            case "BUSINESS_PRODUCT_ORDER_TASK", "BUSINESS_SERVICE_REQUEST_TASK", "GLOBAL_SEARCH_TASK" -> genericTextReply(to, "I have loaded the next step for this business journey.");
            default -> genericTextReply(to, "I am ready for the next step.");
        };
    }

    private ReplyPlan menuReply(String to) {
        List<Section> sections = List.of(
                WhatsappMessageGenerator.createSection(List.of(
                        WhatsappMessageGenerator.createRow("Buy products", "Search and buy items", "menu:buy"),
                        WhatsappMessageGenerator.createRow("Services", "Find or book a service", "menu:services"),
                        WhatsappMessageGenerator.createRow("Track orders", "Check order or booking status", "menu:track"),
                        WhatsappMessageGenerator.createRow("Onboard business", "Add a new business", "menu:onboard"),
                        WhatsappMessageGenerator.createRow("Support", "Talk to a human", "menu:support")
                ), "Main options")
        );
        InteractiveMessageRequest request = WhatsappMessageGenerator.createListMessage(
                to,
                WhatsappMessageGenerator.createTextHeader("Welcome to Brillo"),
                Body.builder().text("Choose the path you want to continue with.").build(),
                Footer.builder().text("Brillo keeps sessions deterministic.").build(),
                "Open menu",
                sections
        );
        return new ReplyPlan("Choose the path you want to continue with.", WhatsappMessageType.LIST, request, TaskSessionStatus.ACTIVE, ConversationStatus.AWAITING_USER, false, TaskDecisionSource.RULE, 1.0d, "Menu ready");
    }

    private ReplyPlan searchProductsReply(String to, String businessId, Map<String, Object> slots) {
        String query = Optional.ofNullable(slots.get("query")).map(Object::toString).orElse("");
        PaginatedResponse<ProductDto> products = productService.getProducts(businessId, 1, DEFAULT_PAGE_SIZE, query, null, null, true, EntityStatus.ACTIVE);
        String reply = products.getItems().isEmpty()
                ? "No matching products were found. Try another search term."
                : "I found " + products.getItems().size() + " product(s): " + products.getItems().stream().map(ProductDto::getName).collect(Collectors.joining(", "));
        return genericTextReply(to, reply);
    }

    private ReplyPlan searchServicesReply(String to, String businessId, Map<String, Object> slots) {
        String query = Optional.ofNullable(slots.get("query")).map(Object::toString).orElse("");
        PaginatedResponse<BusinessServiceDto> services = businessServiceService.listServices(businessId, PageRequest.of(0, DEFAULT_PAGE_SIZE));
        List<BusinessServiceDto> matches = services.getItems().stream()
                .filter(service -> matchesQuery(service, query))
                .toList();
        String reply = matches.isEmpty()
                ? "No matching services were found. Try another search term."
                : "I found " + matches.size() + " service(s): " + matches.stream().map(BusinessServiceDto::getName).collect(Collectors.joining(", "));
        return genericTextReply(to, reply);
    }

    private boolean matchesQuery(BusinessServiceDto service, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        return contains(service.getName(), normalized)
                || contains(service.getDescription(), normalized)
                || contains(service.getCategory(), normalized);
    }

    private ReplyPlan orderTrackingReply(String to, String customerId, Map<String, Object> slots) {
        String orderId = Optional.ofNullable(slots.get("order_id")).map(Object::toString).orElse(null);
        if (orderId != null && !orderId.isBlank()) {
            OrderDto order = orderService.findOrderDetailsForCustomer(orderId, customerId);
            String reply = "Order " + order.getId() + " is currently " + order.getStatus() + " with total " + order.getTotalAmount();
            return genericTextReply(to, reply);
        }
        return genericTextReply(to, "Send your order reference and I will check the status.");
    }

    private ReplyPlan serviceRequestReply(String to, String businessId, Map<String, Object> slots) {
        String serviceId = Optional.ofNullable(slots.get("service_id")).map(Object::toString).orElse(null);
        if (serviceId != null && !serviceId.isBlank()) {
            try {
                BusinessServiceDto service = businessServiceService.findById(serviceId);
                if (!businessId.equals(service.getBusinessId())) {
                    throw new BusinessException("This service does not belong to the current business context.");
                }
                businessServiceService.validateForRequests(service);
                String reply = "I am ready to collect the remaining booking details for " + service.getName() + ".";
                return genericTextReply(to, reply);
            } catch (BusinessException ex) {
                return genericTextReply(to, ex.getMessage());
            }
        }
        return genericTextReply(to, "Share the service you need and I will continue the request flow.");
    }

    private ReplyPlan negotiationReply(String to, Map<String, Object> slots) {
        Object offer = slots.get("price");
        String reply = offer == null
                ? "Send your best offer and I will keep the negotiation open."
                : "I received your offer and will continue the negotiation.";
        return genericTextReply(to, reply);
    }

    private ReplyPlan paymentReply(String to, Map<String, Object> slots) {
        Object amount = slots.get("total_price");
        String reply = amount == null
                ? "I am ready to continue payment once the payable amount is confirmed."
                : "Payment step is ready for " + amount + ".";
        return genericTextReply(to, reply);
    }

    private ReplyPlan supportReply(String to) {
        return new ReplyPlan("A support agent will continue from here shortly.", WhatsappMessageType.TEXT, WhatsappMessageGenerator.createTextMessage(to, "A support agent will continue from here shortly.", false), TaskSessionStatus.ESCALATED, ConversationStatus.HUMAN_TAKEOVER, true, TaskDecisionSource.RULE, 1.0d, "Human support requested");
    }

    private ReplyPlan onboardingReply(String to, String reply) {
        return new ReplyPlan(reply, WhatsappMessageType.CTA_URL, WhatsappMessageGenerator.createTextMessage(to, reply, false), TaskSessionStatus.ACTIVE, ConversationStatus.AWAITING_USER, false, TaskDecisionSource.RULE, 1.0d, "Onboarding link ready");
    }

    private ReplyPlan genericTextReply(String to, String reply) {
        TextMessageRequest request = WhatsappMessageGenerator.createTextMessage(to, reply, false);
        return new ReplyPlan(reply, WhatsappMessageType.TEXT, request, TaskSessionStatus.ACTIVE, ConversationStatus.AWAITING_USER, false, TaskDecisionSource.RULE, 1.0d, null);
    }

    private ReplyPlan buildCompletionReply(String to, String taskKey) {
        return switch (taskKey) {
            case "PRODUCT_SEARCH_TASK", "SERVICE_SEARCH_TASK", "GLOBAL_SEARCH_TASK" -> genericTextReply(to, "Search completed.");
            case "ORDER_TRACKING_TASK", "ORDER_STATUS_QUERY_TASK" -> genericTextReply(to, "Tracking completed.");
            case "PAYMENT_TASK" -> genericTextReply(to, "Payment step completed.");
            default -> genericTextReply(to, "Task completed.");
        };
    }

    private WhatsAppMessageRequest buildFallbackButtons(String to, String replyText) {
        ButtonAction action = WhatsappMessageGenerator.createButtonAction(List.of(
                WhatsappMessageGenerator.createButton("menu:continue", "Continue"),
                WhatsappMessageGenerator.createButton("menu", "Main menu"),
                WhatsappMessageGenerator.createButton("menu:support", "Agent")
        ));
        return WhatsappMessageGenerator.createReplyButtonMessage(
                to,
                WhatsappMessageGenerator.createTextHeader("Choose a safe route"),
                WhatsappMessageGenerator.createBody(replyText),
                WhatsappMessageGenerator.createFooter("Brillo uses deterministic fallbacks."),
                action
        );
    }

    private String buildSlotPrompt(String taskKey, String stateKey, List<String> missingSlots, String normalizedInput) {
        return "I still need " + String.join(", ", missingSlots) + " to continue " + taskKey + " at " + stateKey + ".";
    }

    private boolean isTerminalState(String taskKey, String stateKey) {
        if (stateKey == null) {
            return false;
        }
        List<String> states = catalogLoader.taskStates(taskKey);
        return "COMPLETED".equalsIgnoreCase(stateKey)
                || "CLOSED".equalsIgnoreCase(stateKey)
                || "FAILED".equalsIgnoreCase(stateKey)
                || "CANCELLED".equalsIgnoreCase(stateKey)
                || (!states.isEmpty() && states.getLast().equalsIgnoreCase(stateKey));
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String resolveInput(WhatsappInboundEvent event) {
        if (event.interactiveReplyTitle() != null && !event.interactiveReplyTitle().isBlank()) {
            return event.interactiveReplyTitle();
        }
        if (event.content() != null && !event.content().isBlank()) {
            return event.content();
        }
        if (event.interactiveReplyId() != null && !event.interactiveReplyId().isBlank()) {
            return event.interactiveReplyId();
        }
        return "";
    }

    private boolean isGreeting(String normalizedInput) {
        return normalizedInput.equals("hi")
                || normalizedInput.equals("hello")
                || normalizedInput.equals("hey")
                || normalizedInput.contains("good morning")
                || normalizedInput.contains("good afternoon")
                || normalizedInput.contains("good evening");
    }

    private boolean requestsHuman(WhatsappInboundEvent event, String normalizedInput) {
        String replyId = event.interactiveReplyId();
        return "menu:support".equalsIgnoreCase(replyId)
                || normalizedInput.contains("human")
                || normalizedInput.contains("agent")
                || normalizedInput.contains("support");
    }

    private TaskTurnResult fallbackResult(ConversationTaskSession session, ConversationDto conversation, String normalizedInput, WhatsappInboundEvent event, Map<String, Object> slots, String reason, String to) {
        ReplyPlan plan = genericTextReply(to, reason != null ? reason : "I am not fully sure what you want yet.");
        return new TaskTurnResult(
                Optional.ofNullable(session.getCurrentIntent()).orElse("GREETING"),
                Optional.ofNullable(session.getCurrentTaskKey()).orElse("SHOW_MENU"),
                Optional.ofNullable(session.getCurrentTaskKey()).orElse("SHOW_MENU"),
                Optional.ofNullable(session.getCurrentStateKey()).orElse("SHOW_MENU"),
                0.0d,
                TaskDecisionSource.FALLBACK,
                TaskSessionStatus.FALLBACK,
                ConversationStatus.AWAITING_USER,
                false,
                plan.replyText(),
                plan.type(),
                buildFallbackButtons(to, plan.replyText()),
                slots,
                List.of(),
                reason,
                null
        );
    }

    private TaskTurnResult buildFallbackResult(ConversationTaskSession session, ConversationDto conversation, String normalizedInput, WhatsappInboundEvent event, Map<String, Object> slots, String reason, String to) {
        return fallbackResult(session, conversation, normalizedInput, event, slots, reason, to);
    }

    private TaskTurnResult buildHumanTakeoverResult(ConversationTaskSession session, ConversationDto conversation, String normalizedInput, WhatsappInboundEvent event, Map<String, Object> slots, String to) {
        ReplyPlan plan = supportReply(to);
        return new TaskTurnResult(
                Optional.ofNullable(session.getCurrentIntent()).orElse("SUPPORT_REQUEST"),
                Optional.ofNullable(session.getCurrentTaskKey()).orElse("SUPPORT_TASK"),
                Optional.ofNullable(session.getCurrentTaskKey()).orElse("SUPPORT_TASK"),
                Optional.ofNullable(session.getCurrentStateKey()).orElse("CAPTURE_ISSUE"),
                1.0d,
                TaskDecisionSource.RULE,
                TaskSessionStatus.ESCALATED,
                ConversationStatus.HUMAN_TAKEOVER,
                true,
                plan.replyText(),
                plan.type(),
                plan.message(),
                slots,
                List.of(),
                "Human takeover active",
                null
        );
    }

    private void mergeSlots(Map<String, Object> target, Map<String, Object> source, Set<String> allowedSlots) {
        if (source == null) {
            return;
        }
        source.forEach((key, value) -> {
            if (allowedSlots.contains(key) && value != null && !isEmptyValue(value)) {
                target.put(key, value);
            }
        });
    }

    private record RouteCandidate(String intentKey, String routeTo) {
    }

    private record RouteResolution(
            String intentKey,
            String routeTo,
            TaskDecisionSource source,
            double confidence,
            boolean ambiguous,
            String reason,
            String model,
            TaskSessionStatus taskStatus
    ) {
    }

    private record ReplyPlan(
            String replyText,
            WhatsappMessageType type,
            WhatsAppMessageRequest message,
            TaskSessionStatus sessionStatus,
            ConversationStatus conversationStatus,
            boolean humanTakeover,
            TaskDecisionSource decisionSource,
            double confidence,
            String reason
    ) {
    }

    private record StateAdvance(
            String nextState,
            String replyText,
            WhatsappMessageType presentationType,
            TaskSessionStatus taskSessionStatus,
            ConversationStatus conversationStatus,
            boolean humanTakeover,
            TaskDecisionSource decisionSource,
            double confidence,
            WhatsAppMessageRequest outboundMessage,
            String reason,
            String model
    ) {
    }
}
