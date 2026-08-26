package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationUpsertRequest {
    private String reference;
    private String businessId;
    private String customerId;
    private String whatsappConversationId;
    private String whatsappBusinessNumber;
    private String channelKey;
    private ConversationMode conversationMode;
    private String entryBusinessId;
    private String activeBusinessId;
    private String entrySlug;
    private Boolean marketplaceMode;
    private ConversationStatus status;
    private String lastIntent;
    private String activeTaskKey;
    private Boolean humanTakeover;
    private Instant lastInteractionAt;
    private Instant sessionExpiresAt;
}
