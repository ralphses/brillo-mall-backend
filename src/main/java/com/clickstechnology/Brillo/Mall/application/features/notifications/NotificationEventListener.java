package com.clickstechnology.Brillo.Mall.application.features.notifications;

import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CreateNotificationRequest request) {
        notificationService.sendNotification(request);
    }
}
