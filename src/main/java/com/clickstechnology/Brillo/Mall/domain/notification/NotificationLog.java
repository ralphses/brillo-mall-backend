package com.clickstechnology.Brillo.Mall.domain.notification;

import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationType;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("record_status <> 'DELETED'")
@Table(
        name = "brillo_notification_log",
        indexes = {
                @Index(name = "idx_brillo_notification_log_business", columnList = "business_id"),
                @Index(name = "idx_brillo_notification_log_customer", columnList = "customer_id"),
                @Index(name = "idx_brillo_notification_log_order", columnList = "order_id"),
                @Index(name = "idx_brillo_notification_log_booking", columnList = "booking_id"),
                @Index(name = "idx_brillo_notification_log_payment", columnList = "payment_reference"),
                @Index(name = "idx_brillo_notification_log_request", columnList = "service_request_id"),
                @Index(name = "idx_brillo_notification_log_conversation", columnList = "conversation_id"),
                @Index(name = "idx_brillo_notification_log_status", columnList = "status"),
                @Index(name = "idx_brillo_notification_log_correlation", columnList = "correlation_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_brillo_notification_log_correlation_recipient",
                        columnNames = {"correlation_id", "recipient", "message_medium"}
                )
        }
)
class NotificationLog extends JpaAuditor {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_medium", nullable = false, length = 50)
    private MessageMedium messageMedium;

    @Enumerated(EnumType.STRING)
    @Column(name = "whatsapp_message_type", length = 50)
    private WhatsappMessageType whatsappMessageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "recipient", nullable = false, length = 150)
    private String recipient;

    @Column(name = "recipient_type", length = 50)
    private String recipientType;

    @Column(name = "source_event_type", length = 100)
    private String sourceEventType;

    @Column(name = "source_event_id", length = 100)
    private String sourceEventId;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "business_id", length = 100)
    private String businessId;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(name = "booking_id", length = 100)
    private String bookingId;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "service_request_id", length = 100)
    private String serviceRequestId;

    @Column(name = "conversation_id", length = 100)
    private String conversationId;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "business_initiated", nullable = false)
    private boolean businessInitiated = true;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "payload_json", length = 4000)
    private String payloadJson;
}
