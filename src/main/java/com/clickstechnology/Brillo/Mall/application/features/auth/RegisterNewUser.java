package com.clickstechnology.Brillo.Mall.application.features.auth;

import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.RequestLogService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserInviteService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterResendOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResendOtpResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResponse;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.otp.OtpService;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterNewUser {

    private final UserService userService;
    private final RequestLogService requestLogService;
    private final UserInviteService userInviteService;
    private final OtpService otpService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse execute(RegisterRequest request, HttpServletRequest httpServletRequest) {

        final String username = request.getEmailOrPhone();
        final String ipAddress = httpServletRequest.getRemoteAddr();
        final String userAgent = httpServletRequest.getHeader("User-Agent");

        String logId = requestLogService.logRequest(request, ipAddress, userAgent);

        try {
            MessageMedium messageMedium = AppUtils.resolveMessageMedium(username);

            // Check if user already fully exists
            if (userService.exists(username)) {
                String msg = "Email or Phone is already in use";
                requestLogService.update(logId, msg, RequestStatus.FAILED);
                throw new BusinessException(msg);
            }

            // Check for incomplete user registration
            Optional<UserDto> incompleteUserOpt = userService.getIncompleteUserByUser(username);

            InvitationDto invitedBy;

            // Encode password
            request.setPassword(passwordEncoder.encode(request.getPassword()));

            if (incompleteUserOpt.isPresent()) {
                // If user exists but incomplete → re-use their invite details
                UserDto userDto = incompleteUserOpt.get();
                invitedBy = userInviteService.fromUser(userDto);
                userService.updateUserDetails(request, userDto);

            } else {
                // Otherwise process invitation from request
                invitedBy = userInviteService.processInvitation(request.getInviteCode(), username);

                // Register fresh user only in this case
                userService.registerNewUser(request, invitedBy, false);
            }

            // Send notification if invited to join a school or parent or by an agent.
            if (invitedBy != null) {
                String message = invitedBy.getInvitationMessage()
                        .generateMessage(invitedBy.getInvitedBy(), Optional.empty());

                notificationService.sendNotification(
                        CreateNotificationRequest.builder()
                                .message(message)
                                .build()
                );
            }

            // Generate OTP
            otpService.createOtp(
                    CreateOtpRequest.builder()
                            .messageMedium(messageMedium)
                            .otpType(OtpType.REGISTRATION)
                            .recipients(List.of(username))
                            .build(),
                    httpServletRequest
            );

            // Prepare response
            RegisterResponse response = RegisterResponse.builder()
                    .username(username)
                    .fullName(request.getFullName())
                    .messageMedium(messageMedium)
                    .build();

            requestLogService.update(logId, response, RequestStatus.PROCESSED);
            return response;

        } catch (BusinessException e) {
            requestLogService.update(logId, e.getMessage(), RequestStatus.FAILED);
            throw e;

        } catch (Exception e) {
            requestLogService.update(logId, "Registration failed", RequestStatus.FAILED);
            log.error(":::Unknown error while registering user", e);
            throw new BusinessException("An unexpected error occurred while processing registration");
        }
    }

    public RegisterResendOtpResponse resendOtp(RegisterResendOtpRequest request, HttpServletRequest httpServletRequest) {

        final String ipAddress = httpServletRequest.getRemoteAddr();
        final String userAgent = httpServletRequest.getHeader("User-Agent");

        String logId = requestLogService.logRequest(request, ipAddress, userAgent);

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

            // Prepare response
            RegisterResponse response = RegisterResponse.builder()
                    .username(foundUser.getUsername())
                    .fullName(foundUser.getFullName())
                    .messageMedium(messageMedium)
                    .build();

            requestLogService.update(logId, response, RequestStatus.PROCESSED);
            return new RegisterResendOtpResponse("OTP/Verification link has been resent successfully");

        }
        else {
            requestLogService.update(logId, "Registration Resend OTP failed. Pending user not found", RequestStatus.FAILED);
            throw new BusinessException("Registration Resend OTP failed. Pending user not found");
        }
    }
}
