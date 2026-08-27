package com.clickstechnology.Brillo.Mall.application.dto.request.conversation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConversationActiveBusinessRequest {
    @NotBlank(message = "Active business ID cannot be blank.")
    private String activeBusinessId;
}
