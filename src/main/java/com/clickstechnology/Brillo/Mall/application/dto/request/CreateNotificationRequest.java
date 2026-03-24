package com.clickstechnology.Brillo.Mall.application.dto.request;

import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
}
