package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.VerifyOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder;
import com.clickstechnology.Brillo.Mall.application.dto.response.VerifyOtpResponse;
import com.clickstechnology.Brillo.Mall.infrastructure.otp.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "OTP", description = "OTP generation and verification APIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/otp")
public class OtpController {

    private final OtpService otpService;

    @Operation(summary = "Create OTP", description = "Generates and sends an OTP to the user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "OTP sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<String> createOtp(
            @Valid @RequestBody CreateOtpRequest request,
            HttpServletRequest httpServletRequest) {

        otpService.createOtp(request, httpServletRequest);
        return ResponseBuilder.success("", "Otp sent successfully", 201);
    }

    @Operation(summary = "Verify OTP", description = "Verifies the OTP provided by the user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("verify")
    public ResponseWrapper<VerifyOtpResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpServletRequest) {

        VerifyOtpResponse response = otpService.verifyOtp(request, httpServletRequest);
        return ResponseBuilder.success(response);
    }
}