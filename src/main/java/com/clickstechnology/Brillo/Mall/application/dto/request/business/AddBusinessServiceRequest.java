package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddBusinessServiceRequest {

    @NotBlank(message = "Business ID cannot be blank.")
    private String businessId;

    @NotBlank(message = "Service name cannot be blank.")
    private String name;

    private String description;

    @NotBlank(message = "Category cannot be blank.")
    private String category;

    @NotNull(message = "Pricing type cannot be null.")
    private PricingType pricingType;

    @NotNull(message = "Base price cannot be null.")
    @Positive(message = "Base price must be positive.")
    private BigDecimal basePrice;

    @Positive(message = "Duration must be positive.")
    private Integer durationMinutes;

    private Boolean negotiable = true;

    private Boolean requiresSchedule = false;
}
