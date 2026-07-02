package com.clickstechnology.Brillo.Mall.application.features.runtime;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.InteractiveMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;
import com.clickstechnology.Brillo.Mall.application.enums.FlowSessionStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.application.features.whatsapp.WhatsappInboundEvent;
import com.clickstechnology.Brillo.Mall.application.utils.WhatsappMessageGenerator;
import com.clickstechnology.Brillo.Mall.domain.conversation.ConversationRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.flow.ConversationFlowSession;
import com.clickstechnology.Brillo.Mall.domain.conversation.flow.ConversationFlowSessionRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.task.ConversationTaskSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WhatsAppFlowService {

    private final ConversationRepository conversationRepository;
    private final ConversationFlowSessionRepository flowSessionRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.whatsapp.flows-enabled:true}")
    private boolean flowsEnabled;

    @Value("${app.whatsapp.flow-message-version:3}")
    private String flowMessageVersion;

    @Value("${app.whatsapp.flow-mode:published}")
    private String flowMode;

    @Value("${app.whatsapp.flow-session-hours:24}")
    private Integer flowSessionHours;

    @Transactional
    public Optional<FlowLaunchPlan> maybeLaunchFlow(
            ConversationDto conversation,
            BusinessDto business,
            CustomerDto customer,
            ConversationTaskSession session,
            String taskKey,
            String stateKey,
            Map<String, Object> slots,
            String replyText,
            WhatsappMessageType presentationType,
            WhatsAppMessageRequest fallbackMessage,
            WhatsappInboundEvent event
    ) {
        if (!flowsEnabled || hasSubmission(event)) {
            return Optional.empty();
        }

        FlowSpec spec = resolveSpec(taskKey, stateKey);
        if (spec == null) {
            return Optional.empty();
        }

        String flowToken = buildFlowToken(conversation.getReference(), taskKey, stateKey);
        String correlationReference = buildCorrelationReference(conversation.getReference(), taskKey, stateKey);
        Map<String, Object> payload = buildPayload(conversation, business, customer, session, taskKey, stateKey, slots, correlationReference);

        InteractiveMessageRequest flowRequest = WhatsappMessageGenerator.createFlowMessage(
                event.senderPhone(),
                WhatsappMessageGenerator.createBody(spec.body()),
                WhatsappMessageGenerator.createFooter(spec.footer()),
                spec.flowId(),
                flowToken,
                spec.cta(),
                flowMessageVersion,
                flowMode,
                spec.flowAction(),
                payload,
                null,
                spec.imageHeaderUrl() != null ? WhatsappMessageGenerator.createImageHeader(spec.imageHeaderUrl()) : null,
                WhatsappMessageGenerator.createTextHeader(spec.header()),
                null
        );

        ConversationFlowSession flowSession = upsertLaunchSession(
                conversation,
                business,
                customer,
                session,
                taskKey,
                stateKey,
                spec,
                flowToken,
                correlationReference,
                payload,
                event
        );

        return Optional.of(new FlowLaunchPlan(
                spec.launchReply(),
                WhatsappMessageType.FLOW,
                flowRequest,
                flowSession.getReference(),
                flowToken,
                correlationReference
        ));
    }

    @Transactional
    public Map<String, Object> extractSubmissionSlots(WhatsappInboundEvent event) {
        if (!hasSubmission(event)) {
            return Map.of();
        }

        Map<String, Object> flattened = new LinkedHashMap<>();
        try {
            JsonNode node = objectMapper.readTree(event.flowResponseJson());
            flattenJson(flattened, node, "");
        } catch (Exception ex) {
            return Map.of();
        }

        return flattened;
    }

    @Transactional
    public void markSubmission(
            ConversationDto conversation,
            ConversationTaskSession taskSession,
            WhatsappInboundEvent event,
            Map<String, Object> submissionSlots,
            String lastRouteKey
    ) {
        if (!hasSubmission(event)) {
            return;
        }

        ConversationFlowSession flowSession = flowSessionRepository.findByConversation_Reference(conversation.getReference())
                .orElse(null);
        if (flowSession == null) {
            return;
        }

        if (event.sourceEventId() != null && event.sourceEventId().equals(flowSession.getLastInboundEventId())) {
            return;
        }

        flowSession.setSubmissionPayload(event.flowResponseJson());
        flowSession.setLastInboundEventId(event.sourceEventId());
        flowSession.setLastRouteKey(lastRouteKey);
        flowSession.setFlowStatus(FlowSessionStatus.SUBMITTED);
        flowSession.setSubmittedAt(Instant.now());
        flowSessionRepository.save(flowSession);
    }

    private ConversationFlowSession upsertLaunchSession(
            ConversationDto conversation,
            BusinessDto business,
            CustomerDto customer,
            ConversationTaskSession session,
            String taskKey,
            String stateKey,
            FlowSpec spec,
            String flowToken,
            String correlationReference,
            Map<String, Object> payload,
            WhatsappInboundEvent event
    ) {
        ConversationFlowSession flowSession = flowSessionRepository.findByConversation_Reference(conversation.getReference())
                .orElseGet(() -> ConversationFlowSession.builder()
                        .conversation(conversationRepository.findByReference(conversation.getReference())
                                .orElseThrow(() -> new IllegalStateException("Conversation not found for flow session")))
                        .build());

        flowSession.setBusinessId(business.getId());
        flowSession.setCustomerId(customer.getId());
        flowSession.setWhatsappConversationId(conversation.getWhatsappConversationId());
        flowSession.setFlowId(spec.flowId());
        flowSession.setFlowName(spec.flowName());
        flowSession.setFlowToken(flowToken);
        flowSession.setFlowCta(spec.cta());
        flowSession.setFlowMode(flowMode);
        flowSession.setFlowAction(spec.flowAction());
        flowSession.setFlowMessageVersion(flowMessageVersion);
        flowSession.setLaunchTaskKey(taskKey);
        flowSession.setLaunchStateKey(stateKey);
        flowSession.setLaunchIntentKey(session.getCurrentIntent());
        flowSession.setLaunchPresentationType(WhatsappMessageType.FLOW.getValue());
        flowSession.setCorrelationReference(correlationReference);
        flowSession.setLaunchPayload(serialize(payload));
        flowSession.setFlowStatus(FlowSessionStatus.LAUNCHED);
        flowSession.setLaunchedAt(Instant.now());
        flowSession.setExpiresAt(Instant.now().plus(flowSessionHours, ChronoUnit.HOURS));
        flowSession.setLastRouteKey(taskKey);
        flowSession.setLastInboundEventId(event.whatsappMessageId());
        flowSessionRepository.save(flowSession);
        return flowSession;
    }

    private FlowSpec resolveSpec(String taskKey, String stateKey) {
        if (taskKey == null || stateKey == null) {
            return null;
        }
        return switch (taskKey) {
            case "BUSINESS_ONBOARDING_TASK" -> onboardingSpec(stateKey);
            case "PRODUCT_PURCHASE_TASK" -> checkoutSpec(stateKey);
            case "SERVICE_REQUEST_TASK" -> bookingSpec(stateKey);
            case "SUPPORT_TASK" -> supportSpec(stateKey);
            case "USER_ONBOARDING_TASK" -> userOnboardingSpec(stateKey);
            default -> null;
        };
    }

    private FlowSpec onboardingSpec(String stateKey) {
        if (List.of("COLLECT_BUSINESS_NAME", "COLLECT_BUSINESS_TYPE", "VALIDATE_CATEGORY", "CONFIRMATION").contains(stateKey)) {
            return new FlowSpec(
                    "brillo-business-onboarding",
                    "business_onboarding",
                    "Register your business",
                    "Complete your business details in one guided form.",
                    "Continue",
                    "Complete onboarding",
                    "Fill your business details",
                    null,
                    "submit"
            );
        }
        return null;
    }

    private FlowSpec checkoutSpec(String stateKey) {
        if (List.of("CHECKOUT_INITIATED", "PAYMENT_PENDING").contains(stateKey)) {
            return new FlowSpec(
                    "brillo-product-checkout",
                    "product_checkout",
                    "Complete checkout",
                    "Confirm your delivery and payment details to finish checkout.",
                    "Continue",
                    "Complete checkout",
                    "Confirm checkout details",
                    null,
                    "submit"
            );
        }
        return null;
    }

    private FlowSpec bookingSpec(String stateKey) {
        if (List.of("BOOKING_DETAILS", "NEGOTIATION", "AWAITING_ADMIN_DECISION").contains(stateKey)) {
            return new FlowSpec(
                    "brillo-service-booking",
                    "service_booking",
                    "Complete booking",
                    "Fill in your service booking details to continue.",
                    "Continue",
                    "Complete booking",
                    "Book your service",
                    null,
                    "submit"
            );
        }
        return null;
    }

    private FlowSpec supportSpec(String stateKey) {
        if (List.of("CAPTURE_ISSUE", "COLLECT_CONTEXT").contains(stateKey)) {
            return new FlowSpec(
                    "brillo-support-intake",
                    "support_intake",
                    "Tell us what happened",
                    "Share the details we need to help you faster.",
                    "Continue",
                    "Open support",
                    "Describe your issue",
                    null,
                    "submit"
            );
        }
        return null;
    }

    private FlowSpec userOnboardingSpec(String stateKey) {
        if (List.of("COLLECT_NAME", "CREATE_ACCOUNT", "ASK_BUSINESS_OPTIONAL").contains(stateKey)) {
            return new FlowSpec(
                    "brillo-user-onboarding",
                    "user_onboarding",
                    "Finish your profile",
                    "Set up your profile without leaving WhatsApp.",
                    "Continue",
                    "Complete profile",
                    "Finish setup",
                    null,
                    "submit"
            );
        }
        return null;
    }

    private Map<String, Object> buildPayload(
            ConversationDto conversation,
            BusinessDto business,
            CustomerDto customer,
            ConversationTaskSession session,
            String taskKey,
            String stateKey,
            Map<String, Object> slots,
            String correlationReference
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("conversationReference", conversation.getReference());
        payload.put("businessId", business.getId());
        payload.put("customerId", customer.getId());
        payload.put("taskKey", taskKey);
        payload.put("stateKey", stateKey);
        payload.put("currentIntent", session.getCurrentIntent());
        payload.put("currentTaskKey", session.getCurrentTaskKey());
        payload.put("currentStateKey", session.getCurrentStateKey());
        payload.put("correlationReference", correlationReference);
        payload.put("slots", slots == null ? Map.of() : slots);
        return payload;
    }

    private String buildFlowToken(String conversationReference, String taskKey, String stateKey) {
        return sanitize(conversationReference) + ":" + sanitize(taskKey) + ":" + sanitize(stateKey) + ":" + Instant.now().toEpochMilli();
    }

    private String buildCorrelationReference(String conversationReference, String taskKey, String stateKey) {
        return sanitize(conversationReference) + ":" + sanitize(taskKey) + ":" + sanitize(stateKey);
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private boolean hasSubmission(WhatsappInboundEvent event) {
        return event != null && event.flowResponseJson() != null && !event.flowResponseJson().isBlank();
    }

    private void flattenJson(Map<String, Object> target, JsonNode node, String prefix) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = prefix.isBlank() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenJson(target, entry.getValue(), key);
            });
            return;
        }
        if (node.isArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonNode item : node) {
                if (item.isValueNode()) {
                    values.add(readScalar(item));
                } else {
                    Map<String, Object> nested = new LinkedHashMap<>();
                    flattenJson(nested, item, "");
                    values.add(nested);
                }
            }
            if (!prefix.isBlank()) {
                target.put(prefix, values);
            }
            return;
        }
        if (!prefix.isBlank()) {
            Object value = readScalar(node);
            target.put(prefix, value);
            int lastDot = prefix.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < prefix.length() - 1) {
                String leafKey = prefix.substring(lastDot + 1);
                target.putIfAbsent(leafKey, value);
            }
        }
    }

    private Object readScalar(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        return node.asText();
    }

    private String serialize(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    public record FlowLaunchPlan(
            String replyText,
            WhatsappMessageType presentationType,
            WhatsAppMessageRequest outboundMessage,
            String flowSessionReference,
            String flowToken,
            String correlationReference
    ) {
    }

    private record FlowSpec(
            String flowId,
            String flowName,
            String header,
            String body,
            String cta,
            String launchReply,
            String footer,
            String imageHeaderUrl,
            String flowAction
    ) {
    }
}
