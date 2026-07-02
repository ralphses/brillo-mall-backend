package com.clickstechnology.Brillo.Mall.application.dto.request;

import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationType;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    private String title;
    private String message;
    private NotificationType type;
    private MessageMedium messageMedium;
    private List<String> recipients;

    @Builder.Default
    private WhatsappMessageType whatsappMessageType = WhatsappMessageType.TEXT;
    private String templateName;
    private String sourceEventType;
    private String sourceEventId;
    private String correlationId;
    private String businessId;
    private String customerId;
    private String orderId;
    private String bookingId;
    private String paymentReference;
    private String serviceRequestId;
    private String conversationId;
    private String recipientType;

    @Builder.Default
    private boolean businessInitiated = true;

    @Builder.Default
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
