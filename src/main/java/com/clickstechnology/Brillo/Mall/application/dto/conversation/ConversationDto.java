package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private String id;
    private String reference;
    private String businessId;
    private String customerId;
    private String whatsappConversationId;
    private String whatsappBusinessNumber;
    private ConversationStatus status;
    private String lastIntent;
    private String activeTaskKey;
    private Boolean humanTakeover;
    private Instant lastInteractionAt;
    private Instant sessionExpiresAt;
    private List<MessageDto> messages;
    private Instant createdAt;
    private Instant updatedAt;
}
