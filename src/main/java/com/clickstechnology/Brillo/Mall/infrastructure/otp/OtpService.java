package com.clickstechnology.Brillo.Mall.infrastructure.otp;

import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.VerifyOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.VerifyOtpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

public interface OtpService {
    void createOtp(@Valid CreateOtpRequest request, HttpServletRequest httpServletRequest);

    VerifyOtpResponse verifyOtp(@Valid VerifyOtpRequest request, HttpServletRequest httpServletRequest);

}
