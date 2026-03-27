package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardBusinessRequest {
    private String businessName;
    private String businessCategory;
}
