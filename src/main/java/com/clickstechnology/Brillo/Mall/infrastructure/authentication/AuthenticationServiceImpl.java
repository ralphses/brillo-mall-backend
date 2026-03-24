package com.clickstechnology.Brillo.Mall.infrastructure.authentication;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.RequestLogService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserInviteService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.LoginRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.NewPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterResendOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterVerifyRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.ResetPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.LoginResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.NewPasswordResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResendOtpResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterVerifyResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResetPasswordResponse;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.otp.OtpService;
import com.clickstechnology.Brillo.Mall.infrastructure.utils.AppUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final RequestLogService requestLogService;
    private final UserInviteService userInviteService;
    private final OtpService otpService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest request, HttpServletRequest httpServletRequest) {

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


    @Override
    public RegisterVerifyResponse verifyRegister(RegisterVerifyRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public RegisterResendOtpResponse registerResendOtp(RegisterResendOtpRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public NewPasswordResponse newPassword(NewPasswordRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest) {

    }
}
