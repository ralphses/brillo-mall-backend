package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SupportConversationSummaryDto {
    private String reference;
    private String customerId;
    private String whatsappConversationId;
    private String channelKey;
    private ConversationMode conversationMode;
    private ConversationStatus status;
    private String entryBusinessId;
    private String activeBusinessId;
    private String entrySlug;
    private Boolean marketplaceMode;
    private Boolean humanTakeover;
    private Integer reopenCount;
    private Instant lastReopenedAt;
    private String lastSessionEvent;
    private Instant lastSessionEventAt;
    private String lastInboundMessage;
    private String lastOutboundMessage;
    private Instant lastInboundAt;
    private Instant lastOutboundAt;
    private Instant lastInteractionAt;
    private SupportConversationAssignmentDto assignment;
}
