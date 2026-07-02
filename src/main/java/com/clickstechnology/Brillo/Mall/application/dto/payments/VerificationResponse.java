package com.clickstechnology.Brillo.Mall.application.dto.payments;

import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificationResponse {
    private boolean verified;
    private String message;
    private String reference;
    private PaymentStatus paymentStatus;
    private boolean reconciled;
}
