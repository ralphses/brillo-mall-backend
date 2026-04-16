package com.clickstechnology.Brillo.Mall.infrastructure.otp;

import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.VerifyOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.VerifyOtpResponse;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationType;
import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import com.clickstechnology.Brillo.Mall.application.exception.ApplicationException;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

import static com.clickstechnology.Brillo.Mall.application.utils.AppConstants.SECURE_RANDOM;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final CacheUtil cacheUtil;
    private final AppPropertiesConfig appPropertiesConfig;
    private final NotificationService notificationService;

    @Override
    public void createOtp(CreateOtpRequest request, HttpServletRequest httpServletRequest) {

        String remoteAddr = httpServletRequest.getRemoteAddr();
        String userDevice = httpServletRequest.getHeader("User-Agent");

        List<String> recipients = formatRecipients(
                request.getRecipients(),
                request.getMessageMedium()
        );

        Duration otpDuration = Duration.ofMinutes(appPropertiesConfig.getOtpDuration());

        // Generate 6-digit OTP
        String otp = generateOtp();
        log.info("::Generated OTP: {}", otp);

        // Store OTP per recipient
        for (String recipient :   request.getRecipients()) {

            String cacheKey = buildCacheKey(request.getOtpType(), recipient);

            OtpData otpData = OtpData.builder()
                    .valid(true)
                    .originDevice(userDevice)
                    .origin(remoteAddr)
                    .otp(otp)
                    .owner(recipient)
                    .type(request.getOtpType().name())
                    .build();

            cacheUtil.set(cacheKey, otpData, otpDuration);
        }

        String otpMessage = """
                Your One-Time Password is %s
                """.formatted(otp);

        CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
                .message(otpMessage)
                .type(NotificationType.OTP)
                .title("OTP Message")
                .messageMedium(request.getMessageMedium())
                .recipients(recipients)
                .build();

        notificationService.sendNotification(notificationRequest);
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request, HttpServletRequest httpServletRequest) {

        try {

            String cacheKey = buildCacheKey(request.getOtpType(), request.getUsername());

            OtpData otpData = cacheUtil.get(cacheKey, OtpData.class);

            if (otpData == null) {
                throw new BusinessException("OTP expired or not valid");
            }

            if (!otpData.isValid()) {
                throw new BusinessException("OTP already used or invalid");
            }

            // Validate OTP
            if (!otpData.getOtp().equals(request.getOtp())) {
                throw new BusinessException("Invalid OTP");
            }

            // Validate type
            if (!otpData.getType().equals(request.getOtpType().name())) {
                throw new BusinessException("Invalid OTP");
            }

            // Validate owner (strict match)
            if (!otpData.getOwner().equalsIgnoreCase(request.getUsername())) {
                throw new BusinessException("OTP does not belong to user");
            }

            // Invalidate OTP after success
            cacheUtil.evict(cacheKey);

            return VerifyOtpResponse.builder()
                    .verified(true)
                    .message("OTP verified successfully")
                    .build();

        } catch (ApplicationException | BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(":::Error verifying OTP", ex);
            throw new ApplicationException("Unable to verify OTP", ex);
        }
    }

    private String buildCacheKey(OtpType otpType, String username) {
        return CacheNames.OTP + otpType.name() + ":" + username;
    }

    private String generateOtp() {
        int otp = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", otp);
    }

    private List<String> formatRecipients(List<String> recipients, MessageMedium messageMedium) {

        if (MessageMedium.EMAIL == messageMedium) {
            return recipients;
        }

        return recipients.stream()
                .map(phone -> "234" + phone.substring(phone.length() - 10))
                .toList();
    }
}
