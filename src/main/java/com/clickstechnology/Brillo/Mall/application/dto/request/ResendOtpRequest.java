package com.clickstechnology.Brillo.Mall.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResendOtpRequest {
    private final String emailOrPhone;
}
