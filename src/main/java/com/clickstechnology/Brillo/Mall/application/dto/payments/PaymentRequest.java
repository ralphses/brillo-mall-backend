package com.clickstechnology.Brillo.Mall.application.dto.payments;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentRequest {
    private BigDecimal amount;
    private String email;
    private String currency;
    private String reference;
}