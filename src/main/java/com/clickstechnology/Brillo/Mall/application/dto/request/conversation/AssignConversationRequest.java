package com.clickstechnology.Brillo.Mall.application.dto.request.conversation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignConversationRequest {
    @NotBlank
    private String supportUserId;
    private String reason;
}
