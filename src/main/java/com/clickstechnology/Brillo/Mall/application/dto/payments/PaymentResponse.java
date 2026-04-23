package com.clickstechnology.Brillo.Mall.application.dto.payments;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String authorizationUrl;
    private String accessCode;
    private String reference;
}
