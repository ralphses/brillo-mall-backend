package com.clickstechnology.Brillo.Mall.infrastructure.external.dtos;

import lombok.Data;

@Data
public class PaystackVerificationResponse {
    private boolean status;
    private String message;
}
