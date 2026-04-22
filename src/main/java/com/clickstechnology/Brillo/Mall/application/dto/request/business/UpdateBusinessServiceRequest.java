package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateBusinessServiceRequest {

    private String name;

    private String description;

    @NotBlank(message = "Business ID cannot be blank.")
    private String businessId;

    private String category;

    private PricingType pricingType;

    @Positive(message = "Base price must be positive.")
    private BigDecimal basePrice;

    @Positive(message = "Duration must be positive.")
    private Integer durationMinutes;

    private Boolean negotiable = true;

    private Boolean requiresSchedule = false;
}
