package com.clickstechnology.Brillo.Mall.application.dto.business;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServiceDto {

    private String id;
    private String businessId;
    private String name;
    private String slug;
    private String description;
    private String category;
    private PricingType pricingType;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private boolean negotiable;
    private boolean requiresSchedule;
    private boolean active;
    private EntityStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
