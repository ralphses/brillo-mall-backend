package com.clickstechnology.Brillo.Mall.application.features.auth;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.RequestLogService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserInviteService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.ResendOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.VerifyOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.OtpSentResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.VerifyOtpResponse;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import com.clickstechnology.Brillo.Mall.infrastructure.otp.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterNewUser {

    private final UserService userService;
    private final UserInviteService userInviteService;
    private final OtpService otpService;
    private final NotificationService notificationService;
    private final AuthenticationUtil authenticationUtil;
    private final CacheUtil cacheUtil;

    @LoggableRequest
    public RegisterResponse execute(RegisterRequest request, HttpServletRequest httpServletRequest) {

        final String username = request.getEmailOrPhone();

        MessageMedium messageMedium = AppUtils.resolveMessageMedium(username);

        if (userService.exists(username)) {
            throw new BusinessException("Email or Phone is already in use");
        }

        Optional<UserDto> incompleteUserOpt = userService.getIncompleteUserByUser(username);

        InvitationDto invitedBy;

        request.setPassword(authenticationUtil.encodePassword(request.getPassword()));

        if (incompleteUserOpt.isPresent()) {
            UserDto userDto = incompleteUserOpt.get();
            invitedBy = userInviteService.fromUser(userDto);
            userService.updateUserDetails(request, userDto);
        } else {
            invitedBy = userInviteService.processInvitation(request.getInviteCode(), username);
            userService.registerNewUser(request, invitedBy, false);
        }

        if (invitedBy != null) {
            String message = invitedBy.getInvitationMessage()
                    .generateMessage(invitedBy.getInvitedBy(), Optional.empty());

            notificationService.sendNotification(
                    CreateNotificationRequest.builder()
                            .message(message)
                            .build()
            );
        }

        otpService.createOtp(
                CreateOtpRequest.builder()
                        .messageMedium(messageMedium)
                        .otpType(OtpType.REGISTRATION)
                        .recipients(List.of(username))
                        .build(),
                httpServletRequest
        );

        return RegisterResponse.builder()
                .username(username)
                .fullName(request.getFullName())
                .messageMedium(messageMedium)
                .build();
    }

    @LoggableRequest
    public OtpSentResponse resendOtp(ResendOtpRequest request, HttpServletRequest httpServletRequest) {

        // Check for incomplete user registration
        Optional<UserDto> incompleteUserOpt = userService.getIncompleteUserByUser(request.getEmailOrPhone());
        if (incompleteUserOpt.isPresent()) {

            UserDto foundUser = incompleteUserOpt.get();
            MessageMedium messageMedium = AppUtils.resolveMessageMedium(foundUser.getUsername());

            // Generate OTP
            otpService.createOtp(
                    CreateOtpRequest.builder()
                            .messageMedium(messageMedium)
                            .otpType(OtpType.REGISTRATION)
                            .recipients(List.of(foundUser.getUsername()))
                            .build(),
                    httpServletRequest
            );

            return new OtpSentResponse("OTP/Verification link has been resent successfully");

        }
        else {
            throw new BusinessException("Registration Resend OTP failed. Pending user not found");
        }
    }

    @LoggableRequest
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request, HttpServletRequest httpServletRequest) {

        VerifyOtpResponse verifyOtpResponse = otpService.verifyOtp(request, httpServletRequest);

        if (verifyOtpResponse.isVerified()) {
            String username = request.getUsername();
            Optional<UserDto> incompleteUserByUser = userService.getIncompleteUserByUser(username);

            if (incompleteUserByUser.isPresent()) {
                UserDto userDto = incompleteUserByUser.get();
                userService.completeUserRegistration(userDto);
                final String cacheKey = CacheNames.USER_AUTH + "::" + username;
                cacheUtil.evict(cacheKey);
                return new VerifyOtpResponse(true, "Registration completed successfully. Proceed to login");
            }
        }
        throw new BusinessException("Registration failed. Invalid or expired OTP");
    }
}
