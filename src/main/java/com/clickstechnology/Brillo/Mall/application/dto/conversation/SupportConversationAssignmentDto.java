package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.SupportAssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportConversationAssignmentDto {
    private SupportAssignmentStatus assignmentStatus;
    private String assignedSupportUserId;
    private Instant assignedSupportAt;
    private Instant lastSupportActionAt;
}
