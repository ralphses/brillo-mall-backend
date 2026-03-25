package com.clickstechnology.Brillo.Mall.application.dto.request;

import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    private String otp;
    private OtpType otpType;
    private String username;
}
