package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {
    private String conversationReference;
    private String content;
    private MessageType messageType;
    private String intent;
    private String whatsappMessageId;
    private String transportType;
    private String sourceEventId;
    private String metadata;
}
