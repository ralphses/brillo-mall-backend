package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.request.*;
import com.clickstechnology.Brillo.Mall.application.dto.response.*;
import com.clickstechnology.Brillo.Mall.application.features.auth.AuthenticateUser;
import com.clickstechnology.Brillo.Mall.application.features.auth.RegisterNewUser;
import com.clickstechnology.Brillo.Mall.application.features.auth.UpdatePassword;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@Tag(name = "Authentication", description = "User authentication and account management APIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthenticationController {

    private final RegisterNewUser registerNewUser;
    private final UpdatePassword updatePassword;
    private final AuthenticateUser authenticateUser;

    @Operation(summary = "Register user", description = "Initiates user registration and sends OTP")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registration initiated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest) {

        var response = registerNewUser.execute(request, httpServletRequest);
        return success(response, "Profile creation initiated. Enter OTP to verify your account.", 201);
    }

    @Operation(summary = "Resend registration OTP", description = "Resends OTP for registration")
    @PostMapping("register/resend-otp")
    public ResponseWrapper<OtpSentResponse> registerResendOtp(
            @Valid @RequestBody ResendOtpRequest request,
            HttpServletRequest httpServletRequest) {

        OtpSentResponse response = registerNewUser.resendOtp(request, httpServletRequest);
        return success(response);
    }

    @Operation(summary = "Verify registration OTP", description = "Verifies user registration OTP")
    @PostMapping("register/verify")
    public ResponseWrapper<VerifyOtpResponse> registerVerifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpServletRequest) {

        VerifyOtpResponse response = registerNewUser.verifyOtp(request, httpServletRequest);
        return success(response);
    }

    @Operation(summary = "Initiate password reset", description = "Sends OTP for password reset")
    @PostMapping("reset-password")
    public ResponseWrapper<OtpSentResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpServletRequest) {

        OtpSentResponse response = updatePassword.resetPassword(request, httpServletRequest);
        return success(response);
    }

    @Operation(summary = "Resend reset OTP", description = "Resends OTP for password reset")
    @PostMapping("reset-password/resend-otp")
    public ResponseWrapper<OtpSentResponse> resetResendOtp(
            @Valid @RequestBody ResendOtpRequest request,
            HttpServletRequest httpServletRequest) {

        OtpSentResponse response = updatePassword.resendOtp(request, httpServletRequest);
        return success(response);
    }

    @Operation(summary = "Verify reset OTP", description = "Verifies OTP for password reset")
    @PostMapping("reset-password/verify")
    public ResponseWrapper<VerifyOtpResponse> resetPasswordVerifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpServletRequest) {

        VerifyOtpResponse response = updatePassword.verifyOtp(request, httpServletRequest);
        return success(response);
    }

    @Operation(summary = "Set new password", description = "Sets a new password after OTP verification")
    @PostMapping("new-password")
    public ResponseWrapper<NewPasswordResponse> newPassword(
            @Valid @RequestBody NewPasswordRequest request,
            HttpServletRequest httpServletRequest) {

        NewPasswordResponse response = updatePassword.newPassword(request, httpServletRequest);
        return success(response);
    }

    @Operation(summary = "Change password", description = "Changes password for authenticated user")
    @PostMapping("change-password")
    public ResponseWrapper<NewPasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpServletRequest) {

        NewPasswordResponse response = updatePassword.changePassword(request, httpServletRequest);
        return success(response);
    }

    @Operation(summary = "Login", description = "Authenticates user and returns access token")
    @PostMapping("login")
    public ResponseWrapper<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        LoginResponse response = authenticateUser.login(request, httpServletRequest);
        return success(response);
    }

    @Operation(summary = "Logout", description = "Invalidates user session/token")
    @PostMapping("logout")
    public ResponseWrapper<String> logout(HttpServletRequest httpServletRequest) {

        authenticateUser.logout(httpServletRequest);
        return success();
    }
}