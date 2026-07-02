package com.clickstechnology.Brillo.Mall.application.features.notifications;

import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentLogDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BookingStatus;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationType;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;
import com.clickstechnology.Brillo.Mall.application.enums.ServiceRequestStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationEventPublisher {
    private final NotificationService notificationService;

    public NotificationEventPublisher(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void publishOrderCreated(OrderDto order, CustomerDto customer, BusinessDto business) {
        publishCustomer(
                "Order placed",
                "Your order " + order.getId() + " has been created successfully.",
                NotificationType.INFO,
                "ORDER_CREATED",
                order.getId(),
                customer,
                business,
                null,
                null,
                order.getId(),
                null,
                WhatsappMessageType.TEXT,
                "order_created",
                true,
                metadata("status", OrderStatus.PENDING.name())
        );
        publishBusiness(
                "New order received",
                "Order " + order.getId() + " has been created for your business.",
                NotificationType.ACTION_REQUIRED,
                "ORDER_CREATED",
                order.getId(),
                business,
                customer,
                null,
                null,
                order.getId(),
                null,
                WhatsappMessageType.BUTTON,
                "order_created_business",
                true,
                metadata("buttons", List.of("View order", "Acknowledge"))
        );
    }

    public void publishOrderStatusChanged(OrderDto order, CustomerDto customer, BusinessDto business, OrderStatus previousStatus, OrderStatus currentStatus) {
        String eventType = "ORDER_" + currentStatus.name();
        WhatsappMessageType messageType = chooseMessageType(currentStatus);

        publishCustomer(
                "Order updated",
                "Your order " + order.getId() + " moved from " + previousStatus + " to " + currentStatus + ".",
                notificationTypeForOrderStatus(currentStatus),
                eventType,
                order.getId(),
                customer,
                business,
                null,
                null,
                order.getId(),
                null,
                messageType,
                "order_status_update",
                true,
                metadata("status", currentStatus.name())
        );
        publishBusiness(
                "Order status changed",
                "Order " + order.getId() + " is now " + currentStatus + ".",
                NotificationType.SYSTEM,
                eventType,
                order.getId(),
                business,
                customer,
                null,
                null,
                order.getId(),
                null,
                WhatsappMessageType.TEXT,
                "order_status_business",
                true,
                metadata("status", currentStatus.name())
        );
    }

    public void publishBookingCreated(BookedServiceDto booking, CustomerDto customer, BusinessDto business) {
        publishCustomer(
                "Booking created",
                "Your booking " + booking.getId() + " has been created successfully.",
                NotificationType.INFO,
                "BOOKING_CREATED",
                booking.getId(),
                customer,
                business,
                booking.getId(),
                null,
                null,
                null,
                WhatsappMessageType.TEXT,
                "booking_created",
                true,
                metadata("status", booking.getBookingStatus() != null ? booking.getBookingStatus().name() : null)
        );
        publishBusiness(
                "New booking received",
                "Booking " + booking.getId() + " is waiting for review or fulfillment.",
                NotificationType.ACTION_REQUIRED,
                "BOOKING_CREATED",
                booking.getId(),
                business,
                customer,
                booking.getId(),
                null,
                null,
                null,
                WhatsappMessageType.BUTTON,
                "booking_created_business",
                true,
                metadata("buttons", List.of("View booking", "Acknowledge"))
        );
    }

    public void publishBookingStatusChanged(
            BookedServiceDto booking,
            CustomerDto customer,
            BusinessDto business,
            BookingStatus previousStatus,
            BookingStatus currentStatus
    ) {
        String eventType = "BOOKING_" + currentStatus.name();
        WhatsappMessageType messageType = chooseMessageType(currentStatus);
        NotificationType notificationType = notificationTypeForBookingStatus(currentStatus);

        publishCustomer(
                "Booking updated",
                "Your booking " + booking.getId() + " moved from " + previousStatus + " to " + currentStatus + ".",
                notificationType,
                eventType,
                booking.getId(),
                customer,
                business,
                booking.getId(),
                null,
                null,
                null,
                messageType,
                "booking_status_update",
                true,
                metadata("status", currentStatus.name())
        );
        publishBusiness(
                "Booking status changed",
                "Booking " + booking.getId() + " is now " + currentStatus + ".",
                NotificationType.SYSTEM,
                eventType,
                booking.getId(),
                business,
                customer,
                booking.getId(),
                null,
                null,
                null,
                WhatsappMessageType.TEXT,
                "booking_status_business",
                true,
                metadata("status", currentStatus.name())
        );
    }

    public void publishPaymentUpdate(
            PaymentLogDto paymentLog,
            CustomerDto customer,
            BusinessDto business,
            PaymentStatus paymentStatus,
            String templateName,
            boolean success
    ) {
        String sourceEventType = "PAYMENT_" + paymentStatus.name();
        String payableOrderId = paymentLog.getPayableType() != null && paymentLog.getPayableType().name().equals("ORDER")
                ? paymentLog.getPayableId()
                : null;
        String payableBookingId = paymentLog.getPayableType() != null && paymentLog.getPayableType().name().equals("BOOKING")
                ? paymentLog.getPayableId()
                : null;

        publishCustomer(
                success ? "Payment confirmed" : "Payment update",
                success
                        ? "Your payment " + paymentLog.getPaymentReference() + " has been confirmed."
                        : "Your payment " + paymentLog.getPaymentReference() + " needs attention.",
                success ? NotificationType.PAYMENT : NotificationType.WARNING,
                sourceEventType,
                paymentLog.getPaymentReference(),
                customer,
                business,
                payableOrderId,
                payableBookingId,
                paymentLog.getPaymentReference(),
                null,
                success ? WhatsappMessageType.TEXT : WhatsappMessageType.TEMPLATE,
                templateName,
                true,
                paymentMetadata(paymentLog, paymentStatus)
        );
        publishBusiness(
                success ? "Payment received" : "Payment needs attention",
                "Payment " + paymentLog.getPaymentReference() + " is now " + paymentStatus + ".",
                success ? NotificationType.PAYMENT : NotificationType.WARNING,
                sourceEventType,
                paymentLog.getPaymentReference(),
                business,
                customer,
                payableOrderId,
                payableBookingId,
                paymentLog.getPaymentReference(),
                null,
                WhatsappMessageType.TEXT,
                templateName,
                true,
                paymentMetadata(paymentLog, paymentStatus)
        );
    }

    public void publishServiceRequestCreated(BusinessServiceRequestDto request, CustomerDto customer, BusinessDto business) {
        publishCustomer(
                "Service request received",
                "Your service request " + request.getId() + " has been received.",
                NotificationType.INFO,
                "SERVICE_REQUEST_RECEIVED",
                request.getId(),
                customer,
                business,
                null,
                null,
                null,
                request.getId(),
                WhatsappMessageType.TEXT,
                "service_request_received",
                true,
                metadata("status", request.getRequestStatus() != null ? request.getRequestStatus().name() : null)
        );
        publishBusiness(
                "New service request",
                "A new service request " + request.getId() + " is waiting for review.",
                NotificationType.ACTION_REQUIRED,
                "SERVICE_REQUEST_RECEIVED",
                request.getId(),
                business,
                customer,
                null,
                null,
                null,
                request.getId(),
                WhatsappMessageType.BUTTON,
                "service_request_received_business",
                true,
                metadata("buttons", List.of("View request", "Acknowledge"))
        );
    }

    public void publishServiceRequestUpdated(BusinessServiceRequestDto request, CustomerDto customer, BusinessDto business) {
        ServiceRequestStatus status = request.getRequestStatus();
        String eventType = "SERVICE_REQUEST_" + (status != null ? status.name() : "UPDATED");
        NotificationType notificationType = notificationTypeForServiceRequest(status);
        WhatsappMessageType messageType = chooseMessageType(status);

        publishCustomer(
                "Service request updated",
                "Your service request " + request.getId() + " is now " + status + ".",
                notificationType,
                eventType,
                request.getId(),
                customer,
                business,
                null,
                null,
                null,
                request.getId(),
                messageType,
                status == ServiceRequestStatus.AGREED ? "service_request_agreed" : "service_request_update",
                true,
                metadata("status", status != null ? status.name() : null)
        );
        publishBusiness(
                "Service request update",
                "Service request " + request.getId() + " is now " + status + ".",
                notificationType,
                eventType,
                request.getId(),
                business,
                customer,
                null,
                null,
                null,
                request.getId(),
                WhatsappMessageType.TEXT,
                "service_request_update_business",
                true,
                metadata("status", status != null ? status.name() : null)
        );
    }

    public void publishHumanTakeoverRequested(String recipient, BusinessDto business, CustomerDto customer, String conversationId, String message) {
        if (!StringUtils.hasText(recipient)) {
            return;
        }
        publish(
                buildRequest(
                        "Human takeover requested",
                        message,
                        NotificationType.ACTION_REQUIRED,
                        "HUMAN_TAKEOVER_REQUESTED",
                        conversationId != null ? conversationId : UUID.randomUUID().toString(),
                        recipient,
                        business,
                        customer,
                        null,
                        null,
                        null,
                        null,
                        conversationId,
                        "BUSINESS",
                        WhatsappMessageType.BUTTON,
                        "human_takeover_requested",
                        true,
                        metadata("buttons", List.of("View conversation", "Release"))
                )
        );
    }

    public void publishHumanTakeoverReleased(String recipient, BusinessDto business, CustomerDto customer, String conversationId, String message) {
        if (!StringUtils.hasText(recipient)) {
            return;
        }
        publish(
                buildRequest(
                        "Human takeover released",
                        message,
                        NotificationType.SYSTEM,
                        "HUMAN_TAKEOVER_RELEASED",
                        conversationId != null ? conversationId : UUID.randomUUID().toString(),
                        recipient,
                        business,
                        customer,
                        null,
                        null,
                        null,
                        null,
                        conversationId,
                        "BUSINESS",
                        WhatsappMessageType.TEXT,
                        "human_takeover_released",
                        true,
                        metadata("status", "released")
                )
        );
    }

    public void publishSessionExpired(String recipient, BusinessDto business, CustomerDto customer, String conversationId, String message) {
        if (!StringUtils.hasText(recipient)) {
            return;
        }
        publish(
                buildRequest(
                        "Session expired",
                        message,
                        NotificationType.WARNING,
                        "SESSION_EXPIRED",
                        conversationId != null ? conversationId : UUID.randomUUID().toString(),
                        recipient,
                        business,
                        customer,
                        null,
                        null,
                        null,
                        null,
                        conversationId,
                        "BUSINESS",
                        WhatsappMessageType.TEMPLATE,
                        "session_expired",
                        true,
                        metadata("reason", "session_expired")
                )
        );
    }

    public void publishSupportEscalation(String recipient, BusinessDto business, CustomerDto customer, String message, String sourceEventType) {
        if (!StringUtils.hasText(recipient)) {
            return;
        }
        publish(
                buildRequest(
                        "Support escalation",
                        message,
                        NotificationType.ACTION_REQUIRED,
                        sourceEventType,
                        UUID.randomUUID().toString(),
                        recipient,
                        business,
                        customer,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "BUSINESS",
                        WhatsappMessageType.BUTTON,
                        "support_escalation",
                        true,
                        metadata("buttons", List.of("View conversation", "Release"))
                )
        );
    }

    private void publishCustomer(
            String title,
            String message,
            NotificationType notificationType,
            String sourceEventType,
            String sourceEventId,
            CustomerDto customer,
            BusinessDto business,
            String orderId,
            String bookingId,
            String paymentReference,
            String serviceRequestId,
            WhatsappMessageType messageType,
            String templateName,
            boolean businessInitiated,
            Map<String, Object> metadata
    ) {
        if (customer == null || !StringUtils.hasText(customer.getCustomerPhoneNumber())) {
            return;
        }
        publish(buildRequest(
                title,
                message,
                notificationType,
                sourceEventType,
                sourceEventId,
                customer.getCustomerPhoneNumber(),
                business,
                customer,
                orderId,
                bookingId,
                paymentReference,
                serviceRequestId,
                null,
                "CUSTOMER",
                messageType,
                templateName,
                businessInitiated,
                metadata
        ));
    }

    private void publishBusiness(
            String title,
            String message,
            NotificationType notificationType,
            String sourceEventType,
            String sourceEventId,
            BusinessDto business,
            CustomerDto customer,
            String orderId,
            String bookingId,
            String paymentReference,
            String serviceRequestId,
            WhatsappMessageType messageType,
            String templateName,
            boolean businessInitiated,
            Map<String, Object> metadata
    ) {
        if (business == null) {
            return;
        }
        String recipient = StringUtils.hasText(business.getWhatsappNumber())
                ? business.getWhatsappNumber()
                : business.getPhoneNumber();
        if (!StringUtils.hasText(recipient)) {
            return;
        }
        publish(buildRequest(
                title,
                message,
                notificationType,
                sourceEventType,
                sourceEventId,
                recipient,
                business,
                customer,
                orderId,
                bookingId,
                paymentReference,
                serviceRequestId,
                null,
                "BUSINESS",
                messageType,
                templateName,
                businessInitiated,
                metadata
        ));
    }

    private CreateNotificationRequest buildRequest(
            String title,
            String message,
            NotificationType notificationType,
            String sourceEventType,
            String sourceEventId,
            String recipient,
            BusinessDto business,
            CustomerDto customer,
            String orderId,
            String bookingId,
            String paymentReference,
            String serviceRequestId,
            String conversationId,
            String recipientType,
            WhatsappMessageType messageType,
            String templateName,
            boolean businessInitiated,
            Map<String, Object> metadata
    ) {
        String correlationId = sourceEventType + ":" + sourceEventId + ":" + recipient + ":" + messageType.getValue();
        return CreateNotificationRequest.builder()
                .title(title)
                .message(message)
                .type(notificationType)
                .messageMedium(MessageMedium.PHONE)
                .recipients(List.of(recipient))
                .whatsappMessageType(messageType)
                .templateName(templateName)
                .sourceEventType(sourceEventType)
                .sourceEventId(sourceEventId)
                .correlationId(correlationId)
                .businessId(business != null ? business.getId() : null)
                .customerId(customer != null ? customer.getId() : null)
                .orderId(orderId)
                .bookingId(bookingId)
                .paymentReference(paymentReference)
                .serviceRequestId(serviceRequestId)
                .conversationId(conversationId)
                .recipientType(recipientType)
                .businessInitiated(businessInitiated)
                .metadata(new LinkedHashMap<>(metadata))
                .build();
    }

    private void publish(CreateNotificationRequest request) {
        notificationService.sendNotification(request);
    }

    private WhatsappMessageType chooseMessageType(OrderStatus status) {
        if (status == null) {
            return WhatsappMessageType.TEXT;
        }
        return switch (status) {
            case PENDING, CONFIRMED -> WhatsappMessageType.BUTTON;
            case PAID, SHIPPED, DELIVERED -> WhatsappMessageType.TEXT;
            case CANCELLED, DELETED -> WhatsappMessageType.TEMPLATE;
        };
    }

    private WhatsappMessageType chooseMessageType(BookingStatus status) {
        if (status == null) {
            return WhatsappMessageType.TEXT;
        }
        return switch (status) {
            case PENDING, CONFIRMED -> WhatsappMessageType.BUTTON;
            case IN_PROGRESS, COMPLETED -> WhatsappMessageType.TEXT;
            case CANCELLED -> WhatsappMessageType.TEMPLATE;
            case DELETED -> WhatsappMessageType.TEXT;
        };
    }

    private WhatsappMessageType chooseMessageType(ServiceRequestStatus status) {
        if (status == null) {
            return WhatsappMessageType.TEXT;
        }
        return switch (status) {
            case NEGOTIATING, AGREED -> WhatsappMessageType.BUTTON;
            case BOOKED, REJECTED, EXPIRED, CANCELLED -> WhatsappMessageType.TEMPLATE;
        };
    }

    private NotificationType notificationTypeForOrderStatus(OrderStatus status) {
        return switch (status) {
            case PAID, SHIPPED, DELIVERED -> NotificationType.PAYMENT;
            case CANCELLED, DELETED -> NotificationType.WARNING;
            default -> NotificationType.ACCOUNT_UPDATE;
        };
    }

    private NotificationType notificationTypeForBookingStatus(BookingStatus status) {
        return switch (status) {
            case COMPLETED -> NotificationType.INFO;
            case CANCELLED, DELETED -> NotificationType.WARNING;
            default -> NotificationType.ACCOUNT_UPDATE;
        };
    }

    private NotificationType notificationTypeForServiceRequest(ServiceRequestStatus status) {
        return switch (status) {
            case REJECTED, EXPIRED, CANCELLED -> NotificationType.WARNING;
            case AGREED, BOOKED -> NotificationType.ACCOUNT_UPDATE;
            default -> NotificationType.INFO;
        };
    }

    private Map<String, Object> metadata(Object key, Object value) {
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put(String.valueOf(key), value);
        return metadata;
    }

    private Map<String, Object> paymentMetadata(PaymentLogDto paymentLog, PaymentStatus paymentStatus) {
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("status", paymentStatus.name());
        metadata.put("amount", paymentLog.getAmount());
        metadata.put("payableType", paymentLog.getPayableType() != null ? paymentLog.getPayableType().name() : null);
        metadata.put("payableId", paymentLog.getPayableId());
        return metadata;
    }
}
