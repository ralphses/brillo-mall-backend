package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnboardBusinessRequest {
    private final String businessName;
    private final String businessCategory;
}
