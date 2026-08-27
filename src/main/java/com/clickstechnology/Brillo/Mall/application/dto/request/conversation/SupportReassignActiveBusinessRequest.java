package com.clickstechnology.Brillo.Mall.application.dto.request.conversation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportReassignActiveBusinessRequest {
    @NotBlank
    private String activeBusinessId;
    private String reason;
}
