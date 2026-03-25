package com.clickstechnology.Brillo.Mall.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VerifyOtpResponse {
    private final boolean verified;
    private final String message;
}
