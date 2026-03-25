package com.clickstechnology.Brillo.Mall.application.api.contracts;


import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;

public interface NotificationService {
    void sendNotification(CreateNotificationRequest notificationRequest);
}
