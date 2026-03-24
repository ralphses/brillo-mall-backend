package com.clickstechnology.Brillo.Mall.domain.notification;

import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendNotification(CreateNotificationRequest notificationRequest) {
        log.info("Sending notification to {}", notificationRequest.getRecipients());
        log.info("Notification data : {}", notificationRequest);

    }
}
