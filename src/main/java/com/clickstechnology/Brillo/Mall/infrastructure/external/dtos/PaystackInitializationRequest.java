package com.clickstechnology.Brillo.Mall.infrastructure.external.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaystackInitializationRequest {
    private BigDecimal amount;
    private String email;
    private String currency;
    private String reference;
}
