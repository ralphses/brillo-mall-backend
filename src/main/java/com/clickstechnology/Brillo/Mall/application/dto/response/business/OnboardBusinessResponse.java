package com.clickstechnology.Brillo.Mall.application.dto.response.business;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardBusinessResponse {
    private String message;
    private String storefrontLink;
    private String sharedWhatsappLink;
}
