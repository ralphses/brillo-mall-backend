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
public class SupportConversationNoteDto {
    private String reference;
    private String actorUserId;
    private String content;
    private Instant createdAt;
}
