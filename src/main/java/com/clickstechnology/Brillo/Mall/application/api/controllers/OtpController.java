package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.VerifyOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.ApiResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder;
import com.clickstechnology.Brillo.Mall.application.dto.response.VerifyOtpResponse;
import com.clickstechnology.Brillo.Mall.infrastructure.otp.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/otp")
public class OtpController {
    private final OtpService otpService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<String> createOtp(
            @Valid @RequestBody CreateOtpRequest request, HttpServletRequest httpServletRequest) {
        otpService.createOtp(request, httpServletRequest);
        return ResponseBuilder.success("", "Otp sent successfully", 201);
    }

    @PostMapping("verify")
    public ApiResponse<VerifyOtpResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpServletRequest) {
        VerifyOtpResponse response = otpService.verifyOtp(request, httpServletRequest);
        return ResponseBuilder.success(response);
    }
}
