package com.clickstechnology.Brillo.Mall.domain.notification;

import com.clickstechnology.Brillo.Mall.application.api.contracts.MessageSendService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.DocumentMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.ImageMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.InteractiveMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.TemplateMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.TextMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Body;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.ButtonAction;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Footer;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Header;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.ImageMessage;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Media;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Section;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Template;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationType;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.application.exception.ApplicationException;
import com.clickstechnology.Brillo.Mall.application.utils.WhatsappMessageGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final MessageSendService messageSendService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void sendNotification(CreateNotificationRequest notificationRequest) {
        if (notificationRequest.getRecipients() == null || notificationRequest.getRecipients().isEmpty()) {
            return;
        }

        String correlationId = StringUtils.hasText(notificationRequest.getCorrelationId())
                ? notificationRequest.getCorrelationId()
                : UUID.randomUUID().toString();

        for (String recipient : notificationRequest.getRecipients()) {
            NotificationLog notificationLog = findOrCreate(notificationRequest, recipient, correlationId);
            if (notificationLog.getStatus() == NotificationStatus.SENT) {
                continue;
            }

            notificationLog.setAttempts(notificationLog.getAttempts() == null ? 1 : notificationLog.getAttempts() + 1);
            notificationLog.setStatus(NotificationStatus.PENDING);
            notificationLog.setLastError(null);
            notificationLog.setPayloadJson(serialize(notificationRequest));
            notificationLogRepository.save(notificationLog);

            try {
                if (notificationRequest.getMessageMedium() == MessageMedium.PHONE) {
                    WhatsAppMessageRequest messageRequest = buildWhatsAppMessage(notificationRequest, recipient);
                    messageSendService.sendMessage(messageRequest);
                } else {
                    log.info("Email notification recorded for {} with title {}", recipient, notificationRequest.getTitle());
                }

                notificationLog.setStatus(NotificationStatus.SENT);
                notificationLog.setSentAt(Instant.now());
            } catch (Exception e) {
                notificationLog.setStatus(NotificationStatus.RETRYABLE);
                notificationLog.setLastError(e.getMessage());
                log.warn("Notification delivery failed for recipient {}", recipient, e);
            }

            notificationLogRepository.save(notificationLog);
        }
    }

    private NotificationLog findOrCreate(CreateNotificationRequest request, String recipient, String correlationId) {
        return notificationLogRepository
                .findByCorrelationIdAndRecipientAndMessageMediumAndRecordStatus(
                        correlationId,
                        recipient,
                        request.getMessageMedium(),
                        com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor.RecordStatus.ACTIVE)
                .orElseGet(() -> notificationLogRepository.save(NotificationLog.builder()
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .type(request.getType() != null ? request.getType() : NotificationType.INFO)
                        .messageMedium(request.getMessageMedium())
                        .whatsappMessageType(request.getWhatsappMessageType())
                        .recipient(recipient)
                        .recipientType(request.getRecipientType())
                        .sourceEventType(request.getSourceEventType())
                        .sourceEventId(request.getSourceEventId())
                        .correlationId(correlationId)
                        .businessId(request.getBusinessId())
                        .customerId(request.getCustomerId())
                        .orderId(request.getOrderId())
                        .bookingId(request.getBookingId())
                        .paymentReference(request.getPaymentReference())
                        .serviceRequestId(request.getServiceRequestId())
                        .conversationId(request.getConversationId())
                        .templateName(request.getTemplateName())
                        .businessInitiated(request.isBusinessInitiated())
                        .status(NotificationStatus.PENDING)
                        .attempts(0)
                        .payloadJson(serialize(request))
                        .build()));
    }

    private WhatsAppMessageRequest buildWhatsAppMessage(CreateNotificationRequest request, String recipient) {
        WhatsappMessageType messageType = request.getWhatsappMessageType() != null
                ? request.getWhatsappMessageType()
                : WhatsappMessageType.TEXT;

        return switch (messageType) {
            case TEMPLATE -> buildTemplateMessage(request, recipient);
            case BUTTON -> buildButtonMessage(request, recipient);
            case LIST -> buildListMessage(request, recipient);
            case CTA_URL -> buildCtaMessage(request, recipient);
            case IMAGE -> buildImageMessage(request, recipient);
            case DOCUMENT -> buildDocumentMessage(request, recipient);
            default -> WhatsappMessageGenerator.createTextMessage(recipient, buildBody(request), false);
        };
    }

    private WhatsAppMessageRequest buildTemplateMessage(CreateNotificationRequest request, String recipient) {
        String templateName = StringUtils.hasText(request.getTemplateName())
                ? request.getTemplateName()
                : "status_update";
        return TemplateMessageRequest.builder()
                .to(recipient)
                .template(Template.builder()
                        .name(templateName)
                        .language(Template.Language.builder().code("en").build())
                        .build())
                .build();
    }

    private WhatsAppMessageRequest buildButtonMessage(CreateNotificationRequest request, String recipient) {
        List<String> buttonLabels = resolveStringList(request.getMetadata().get("buttons"), List.of("Acknowledge", "View details"));
        List<com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Button> buttons = new ArrayList<>();
        for (int i = 0; i < buttonLabels.size(); i++) {
            buttons.add(WhatsappMessageGenerator.createButton("notification:" + i, buttonLabels.get(i)));
        }
        return WhatsappMessageGenerator.createReplyButtonMessage(
                recipient,
                WhatsappMessageGenerator.createTextHeader(request.getTitle() != null ? request.getTitle() : "Notification"),
                WhatsappMessageGenerator.createBody(buildBody(request)),
                Footer.builder().text("Brillo notifications").build(),
                ButtonAction.builder().buttons(buttons).build()
        );
    }

    private WhatsAppMessageRequest buildListMessage(CreateNotificationRequest request, String recipient) {
        List<String> options = resolveStringList(request.getMetadata().get("options"), List.of("View details"));
        Section section = WhatsappMessageGenerator.createSection(
                options.stream()
                        .map(option -> WhatsappMessageGenerator.createRow(option, request.getMessage(), "notification:" + option.toLowerCase().replace(' ', '_')))
                        .toList(),
                request.getTitle() != null ? request.getTitle() : "Updates"
        );
        return WhatsappMessageGenerator.createListMessage(
                recipient,
                WhatsappMessageGenerator.createTextHeader(request.getTitle() != null ? request.getTitle() : "Notification"),
                Body.builder().text(buildBody(request)).build(),
                Footer.builder().text("Brillo notifications").build(),
                "Open",
                List.of(section)
        );
    }

    private WhatsAppMessageRequest buildCtaMessage(CreateNotificationRequest request, String recipient) {
        String actionUrl = String.valueOf(request.getMetadata().getOrDefault("actionUrl", "https://brillo.example"));
        return WhatsappMessageGenerator.createCallToActionMessage(
                recipient,
                Body.builder().text(buildBody(request)).build(),
                Footer.builder().text("Brillo notifications").build(),
                actionUrl,
                String.valueOf(request.getMetadata().getOrDefault("actionLabel", "Continue")),
                null,
                null,
                WhatsappMessageGenerator.createTextHeader(request.getTitle() != null ? request.getTitle() : "Notification"),
                null
        );
    }

    private WhatsAppMessageRequest buildImageMessage(CreateNotificationRequest request, String recipient) {
        String imageUrl = String.valueOf(request.getMetadata().getOrDefault("imageUrl", ""));
        return WhatsappMessageGenerator.createImageMessage(
                recipient,
                ImageMessage.builder().link(imageUrl).caption(buildBody(request)).build()
        );
    }

    private WhatsAppMessageRequest buildDocumentMessage(CreateNotificationRequest request, String recipient) {
        String documentUrl = String.valueOf(request.getMetadata().getOrDefault("documentUrl", ""));
        return DocumentMessageRequest.builder()
                .to(recipient)
                .document(Media.builder().link(documentUrl).caption(buildBody(request)).build())
                .build();
    }

    private String buildBody(CreateNotificationRequest request) {
        if (StringUtils.hasText(request.getMessage())) {
            return request.getMessage();
        }
        return request.getTitle() != null ? request.getTitle() : "Notification";
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Unable to serialize notification payload", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> resolveStringList(Object value, List<String> fallback) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return List.of(text);
        }
        return fallback;
    }
}
