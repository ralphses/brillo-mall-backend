package com.clickstechnology.Brillo.Mall.infrastructure.otp;

import com.clickstechnology.Brillo.Mall.application.api.contracts.NotificationService;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateNotificationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.VerifyOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.VerifyOtpResponse;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.NotificationType;
import com.clickstechnology.Brillo.Mall.application.exception.ApplicationException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static com.clickstechnology.Brillo.Mall.application.utils.AppConstants.SECURE_RANDOM;

@Slf4j
@Service
@RequiredArgsConstructor
class OtpServiceImpl implements OtpService {

    private final CacheUtil cacheUtil;
    private final AppConfig appConfig;
    private final NotificationService notificationService;

    @Override
    public void createOtp(CreateOtpRequest request, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String userDevice = httpServletRequest.getHeader("User-Agent");
        List<String> recipients = formatRecipients(request.getRecipients(), request.getMessageMedium());

        long otpDuraion = Duration.ofMinutes(appConfig.getOtpDuration()).toSeconds();
        String otpCode = generateSecureOtp(remoteAddr, userDevice);
        String otp = otpCode.split("-")[0];
        String owner = String.join(",", recipients);

        String otpId = otpCode.split("-")[1];
        OtpData otpData = OtpData.builder()
                .otpId(otpId)
                .valid(true)
                .originDevice(userDevice)
                .origin(remoteAddr)
                .otp(otp)
                .owner(owner)
                .type(request.getOtpType().name())
                .build();
        cacheUtil.set(CacheNames.OTP + otpId, otpData, otpDuraion);

        String otpMessage = """
                Your One-Time-Password is %s
                """.formatted(otpCode);

        CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
                .message(otpMessage)
                .type(NotificationType.OTP)
                .title("OTP Message")
                .messageMedium(request.getMessageMedium())
                .recipients(recipients)
                .build();

        notificationService.sendNotification(notificationRequest);
    }

    private List<String> formatRecipients(List<String> recipients, MessageMedium messageMedium) {

        if (MessageMedium.EMAIL == messageMedium) {
            return recipients;
        }
        return recipients.stream().map(phone ->
                "234" + phone.substring(phone.length() - 10))
                .toList();
    }

    private String generateSecureOtp(String remoteAddr, String userDevice) {

        int otp = SECURE_RANDOM.nextInt(1_000_000);
        String otpString = String.format("%06d", otp);

        String entropySource = remoteAddr + ":" + userDevice + ":" + System.nanoTime();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(entropySource.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();

            String extraEntropy = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(Arrays.copyOf(hash, 4));

            return otpString + "-" + extraEntropy;
        } catch (Exception e) {
            log.error(":::Error generating Secure OTP String", e);
            throw new ApplicationException("Unable to generate secure OTP hash", e);
        }
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }
}
