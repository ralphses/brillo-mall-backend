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

    /**
     * Initiates the password reset process for a user.
     * <p>
     * This method finds a user by their email or phone number, determines the appropriate
     * medium for sending a One-Time Password (OTP), and then triggers the OTP generation
     * and sending process for password reset purposes.
     *
     * @param request The request object containing the user's email or phone number.
     * @param httpServletRequest The HTTP servlet request, used for logging and context.
     * @return An {@link OtpSentResponse} indicating that the password reset OTP has been sent.
     */
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

    /**
     * Sets a new password for a user after verifying the OTP.
     * <p>
     * This method validates the OTP provided by the user, checks if the OTP is valid and not expired,
     * and then updates the user's password with the new one provided in the request.
     *
     * @param request The request object containing the user's email or phone number, OTP, and new password.
     * @param httpServletRequest The HTTP servlet request, used for logging and context.
     * @return A {@link NewPasswordResponse} indicating that the password has been updated successfully.
     */
    @LoggableRequest
    public NewPasswordResponse newPassword(NewPasswordRequest request, HttpServletRequest httpServletRequest) {
        String cacheKey = getCacheKey(request.getEmailOrPhone(), OtpType.PASSWORD_RESET);
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

    /**
     * Allows an authenticated user to change their password.
     * <p>
     * This method retrieves the authenticated user's username from the request, validates
     * that the new password and its confirmation match, and then delegates the password
     * change logic to the user service.
     *
     * @param request The request object containing the current and new passwords.
     * @param httpServletRequest The HTTP servlet request, used to identify the authenticated user.
     * @return A {@link NewPasswordResponse} indicating that the password was changed successfully.
     * @throws UnauthorizedUserException if the user is not authenticated.
     */
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

    /**
     * Verifies a One-Time Password (OTP) for a given user and OTP type.
     * <p>
     * This method delegates the OTP verification to the {@link OtpService}. If the OTP is
     * successfully verified, it caches the verification status for a short duration (5 minutes)
     * to allow the user to proceed with a subsequent action, such as setting a new password.
     *
     * @param request The request object containing the username, OTP, and OTP type.
     * @param httpServletRequest The HTTP servlet request, used for logging and context.
     * @return A {@link VerifyOtpResponse} indicating the result of the OTP verification.
     * @throws BusinessException if the provided OTP is invalid or has expired.
     */
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

        String cacheKey = getCacheKey(request.getUsername(), request.getOtpType());
        cacheUtil.set(cacheKey, verifyOtpResponse, Duration.ofMinutes(5));

        return verifyOtpResponse;
    }

    private String getCacheKey(String username, OtpType otpType) {
        return CacheNames.OTP_CONTINUE + username + otpType;
    }
}