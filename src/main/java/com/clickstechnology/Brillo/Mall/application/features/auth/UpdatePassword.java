package com.clickstechnology.Brillo.Mall.application.features.auth;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.projections.AuthUser;
import com.clickstechnology.Brillo.Mall.application.dto.request.ChangePasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.NewPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.ResendOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.ResetPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.VerifyOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.NewPasswordResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.OtpSentResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.VerifyOtpResponse;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.clickstechnology.Brillo.Mall.infrastructure.otp.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePassword {

    private final UserService userService;
    private final OtpService otpService;
    private final CacheUtil cacheUtil;
    private final AuthenticationUtil authenticationUtil;

    @LoggableRequest
    public OtpSentResponse resetPassword(ResetPasswordRequest request, HttpServletRequest httpServletRequest) {

        AuthUser user = userService.findUserByUsername(request.getEmailOrPhone());

        MessageMedium messageMedium = AppUtils.resolveMessageMedium(user.getUsername());

        // Generate OTP
        otpService.createOtp(
                CreateOtpRequest.builder()
                        .messageMedium(messageMedium)
                        .otpType(OtpType.PASSWORD_RESET)
                        .recipients(List.of(user.getUsername()))
                        .build(),
                httpServletRequest
        );
        return new OtpSentResponse("Password reset initiated successfully. Kindly enter OTP sent to your phone/email");
    }

    @LoggableRequest
    public NewPasswordResponse newPassword(NewPasswordRequest request, HttpServletRequest httpServletRequest) {
        String cacheKey = CacheNames.OTP_CONTINUUE + request.getEmailOrPhone() + OtpType.PASSWORD_RESET;
        if (!cacheUtil.exists(cacheKey)) {
            throw new BusinessException("Invalid or unknown request. Kindly initiate the process again.");
        }

        AppUtils.validatePasswordChange(request.getPassword(), request.getConfirmPassword());

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPassword(authenticationUtil.encodePassword(request.getPassword()));
        registerRequest.setEmailOrPhone(request.getEmailOrPhone());

        UserDto userDto = userService.findByUsername(registerRequest.getEmailOrPhone());
        userService.updateUserDetails(registerRequest, userDto);

        cacheUtil.evict(cacheKey);

        return new NewPasswordResponse("Password updated successfully.");
    }

    @LoggableRequest
    public NewPasswordResponse changePassword(ChangePasswordRequest request, HttpServletRequest httpServletRequest) {

        String username = Optional.ofNullable(authenticationUtil.getAuthenticatedUsername(httpServletRequest))
                .orElseThrow(UnauthorizedUserException::new);

        AppUtils.validatePasswordChange(request.getNewPassword(), request.getConfirmNewPassword());

        userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());

        return new NewPasswordResponse("Password changed successfully.");
    }

    public OtpSentResponse resendOtp(@Valid ResendOtpRequest request, HttpServletRequest httpServletRequest) {
        return resetPassword(new ResetPasswordRequest(request.getEmailOrPhone()), httpServletRequest);
    }

    @LoggableRequest
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request, HttpServletRequest httpServletRequest) {
        VerifyOtpResponse verifyOtpResponse = otpService.verifyOtp(VerifyOtpRequest.builder()
                        .username(request.getUsername())
                        .otpType(request.getOtpType())
                        .otp(request.getOtp())
                        .build(),
                httpServletRequest);
        if (!verifyOtpResponse.isVerified()) {
            throw new BusinessException("OTP invalid or expired");
        }

        String cacheKey = CacheNames.OTP_CONTINUUE + request.getUsername() + request.getOtpType();
        cacheUtil.set(cacheKey, verifyOtpResponse, Duration.ofMinutes(5));

        return verifyOtpResponse;
    }
}
