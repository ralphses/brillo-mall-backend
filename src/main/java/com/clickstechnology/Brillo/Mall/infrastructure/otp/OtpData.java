package com.clickstechnology.Brillo.Mall.infrastructure.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpData {
    private String otp;
    private String otpId;
    private String origin;
    private boolean valid;
    private String originDevice;
    private String type;
    private String owner;
}
