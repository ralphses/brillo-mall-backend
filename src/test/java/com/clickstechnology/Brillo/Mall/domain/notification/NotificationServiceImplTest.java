package com.clickstechnology.Brillo.Mall.domain.notification;

import com.clickstechnology.Brillo.Mall.application.api.contracts.MessageSendService;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsappResponse;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationType;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private MessageSendService messageSendService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void sendNotification_dedupesByCorrelationIdAndRecipient() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Order update")
                .message("Your order has been paid.")
                .type(NotificationType.PAYMENT)
                .messageMedium(MessageMedium.PHONE)
                .whatsappMessageType(WhatsappMessageType.TEXT)
                .recipients(List.of("2348011111111"))
                .correlationId("ORDER_PAID:ORD-1:2348011111111:text")
                .sourceEventType("ORDER_PAID")
                .sourceEventId("ORD-1")
                .businessId("biz-1")
                .customerId("cust-1")
                .orderId("ORD-1")
                .metadata(Map.of("status", "PAID"))
                .build();

        NotificationLog existing = NotificationLog.builder()
                .title("Order update")
                .message("Your order has been paid.")
                .type(NotificationType.PAYMENT)
                .messageMedium(MessageMedium.PHONE)
                .whatsappMessageType(WhatsappMessageType.TEXT)
                .recipient("2348011111111")
                .recipientType("CUSTOMER")
                .sourceEventType("ORDER_PAID")
                .sourceEventId("ORD-1")
                .correlationId("ORDER_PAID:ORD-1:2348011111111:text")
                .businessId("biz-1")
                .customerId("cust-1")
                .orderId("ORD-1")
                .status(NotificationStatus.SENT)
                .attempts(1)
                .build();
        existing.setRecordStatus(JpaAuditor.RecordStatus.ACTIVE);

        when(notificationLogRepository.findByCorrelationIdAndRecipientAndMessageMediumAndRecordStatus(
                eq(request.getCorrelationId()),
                eq("2348011111111"),
                eq(MessageMedium.PHONE),
                eq(JpaAuditor.RecordStatus.ACTIVE)
        )).thenReturn(Optional.empty(), Optional.of(existing));
        when(notificationLogRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageSendService.sendMessage(any())).thenReturn(new WhatsappResponse());
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"title\":\"Order update\"}");

        notificationService.sendNotification(request);
        notificationService.sendNotification(request);

        verify(messageSendService, times(1)).sendMessage(any());
        verify(notificationLogRepository, times(2)).findByCorrelationIdAndRecipientAndMessageMediumAndRecordStatus(
                eq(request.getCorrelationId()),
                eq("2348011111111"),
                eq(MessageMedium.PHONE),
                eq(JpaAuditor.RecordStatus.ACTIVE)
        );
    }

    @Test
    void sendNotification_persistsPayloadAndWhatsappType() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Booking update")
                .message("Your booking is now confirmed.")
                .type(NotificationType.ACCOUNT_UPDATE)
                .messageMedium(MessageMedium.PHONE)
                .whatsappMessageType(WhatsappMessageType.TEXT)
                .recipients(List.of("2348022222222"))
                .correlationId("BOOKING_CONFIRMED:BK-1:2348022222222:text")
                .sourceEventType("BOOKING_CONFIRMED")
                .sourceEventId("BK-1")
                .businessId("biz-2")
                .customerId("cust-2")
                .bookingId("BK-1")
                .metadata(Map.of("status", "CONFIRMED"))
                .build();

        when(notificationLogRepository.findByCorrelationIdAndRecipientAndMessageMediumAndRecordStatus(
                eq(request.getCorrelationId()),
                eq("2348022222222"),
                eq(MessageMedium.PHONE),
                eq(JpaAuditor.RecordStatus.ACTIVE)
        )).thenReturn(Optional.empty());
        when(notificationLogRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageSendService.sendMessage(any())).thenReturn(new WhatsappResponse());
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"bookingId\":\"BK-1\"}");

        notificationService.sendNotification(request);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(3)).save(captor.capture());
        NotificationLog saved = captor.getAllValues().get(0);
        assertThat(saved.getType()).isEqualTo(NotificationType.ACCOUNT_UPDATE);
        assertThat(saved.getWhatsappMessageType()).isEqualTo(WhatsappMessageType.TEXT);
        assertThat(saved.getPayloadJson()).contains("\"bookingId\":\"BK-1\"");
    }
}
