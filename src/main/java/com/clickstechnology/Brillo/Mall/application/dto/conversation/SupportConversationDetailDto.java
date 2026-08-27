package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.TaskSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SupportConversationDetailDto extends SupportConversationSummaryDto {
    private String lastIntent;
    private String activeTaskKey;
    private String currentStateKey;
    private TaskSessionStatus taskStatus;
    private Instant sessionExpiresAt;
    private List<MessageDto> recentMessages;
    private List<SupportConversationNoteDto> latestNotes;
    private List<SupportConversationEventDto> history;
}
