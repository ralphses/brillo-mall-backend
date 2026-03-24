package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.request.ChangePasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.LoginRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.NewPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterResendOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.ResetPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.ApiResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.LoginResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.NewPasswordResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResendOtpResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResetPasswordResponse;
import com.clickstechnology.Brillo.Mall.application.features.auth.AuthenticateUser;
import com.clickstechnology.Brillo.Mall.application.features.auth.RegisterNewUser;
import com.clickstechnology.Brillo.Mall.application.features.auth.UpdatePassword;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthenticationController {

    private final RegisterNewUser registerNewUser;
    private final UpdatePassword updatePassword;
    private final AuthenticateUser authenticateUser;

    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest httpServletRequest) {
        var response = registerNewUser.execute(request, httpServletRequest);
        return success(response, "Profile creation initiated. Enter OTP to verify your account.", 201);
    }

    @PostMapping("register/resend-otp")
    public ApiResponse<RegisterResendOtpResponse> registerResendOtp(
            @Valid @RequestBody RegisterResendOtpRequest request, HttpServletRequest httpServletRequest) {
        RegisterResendOtpResponse response = registerNewUser.resendOtp(request, httpServletRequest);
        return success(response);
    }

    @PostMapping("reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpServletRequest) {
        ResetPasswordResponse response = updatePassword.resetPassword(request, httpServletRequest);
        return success(response);
    }

    @PostMapping("new-password")
    public ApiResponse<NewPasswordResponse> newPassword(
            @Valid @RequestBody NewPasswordRequest request, HttpServletRequest httpServletRequest) {
        NewPasswordResponse response = updatePassword.newPassword(request, httpServletRequest);
        return success(response);
    }

    @PostMapping("change-password")
    public ApiResponse<NewPasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpServletRequest) {
        NewPasswordResponse response = updatePassword.changePassword(request, httpServletRequest);
        return success(response);
    }

    @PostMapping("login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        LoginResponse response = authenticateUser.login(request, httpServletRequest);
        return success(response);
    }

    @PostMapping("logout")
    public ApiResponse<String> logout(HttpServletRequest httpServletRequest) {
        authenticateUser.logout(httpServletRequest);
        return success();
    }
}
