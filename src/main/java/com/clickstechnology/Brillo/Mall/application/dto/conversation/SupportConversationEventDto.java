package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportConversationEventDto {
    private String reference;
    private String eventType;
    private String actorUserId;
    private String reason;
    private String fromAssignedSupportUserId;
    private String toAssignedSupportUserId;
    private String fromActiveBusinessId;
    private String toActiveBusinessId;
    private String metadata;
    private Instant createdAt;
}
