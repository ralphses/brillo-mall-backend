package com.clickstechnology.Brillo.Mall.infrastructure.external;

import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentProcessor;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentRequest;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentResponse;
import com.clickstechnology.Brillo.Mall.application.dto.payments.VerificationResponse;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;
import com.clickstechnology.Brillo.Mall.application.exception.ApplicationException;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.features.payments.ManagePayments;
import com.clickstechnology.Brillo.Mall.infrastructure.external.dtos.PaymentProcessorNames;
import com.clickstechnology.Brillo.Mall.infrastructure.external.dtos.PaystackInitializationRequest;
import com.clickstechnology.Brillo.Mall.infrastructure.external.dtos.PaystackInitializationResponse;
import com.clickstechnology.Brillo.Mall.infrastructure.external.dtos.PaystackVerificationResponse;
import com.clickstechnology.Brillo.Mall.infrastructure.external.dtos.PaystackWebhookEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service("paystackPaymentProcessor")
@RequiredArgsConstructor
public class PaystackPaymentProcessor implements PaymentProcessor {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ManagePayments managePayments;

    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    @Value("${paystack.api.base-url}")
    private String paystackApiBaseUrl;

    @Override
    public PaymentResponse initializePayment(final PaymentRequest paymentRequest) {
        String url = paystackApiBaseUrl + "/transaction/initialize";

        PaystackInitializationRequest request = PaystackInitializationRequest.builder()
                .amount(paymentRequest.getAmount().multiply(new BigDecimal(100))) // Paystack expects amount in kobo
                .email(paymentRequest.getEmail())
                .currency(paymentRequest.getCurrency())
                .reference(paymentRequest.getReference())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + paystackSecretKey);

        HttpEntity<PaystackInitializationRequest> entity = new HttpEntity<>(request, headers);

        try {
            PaystackInitializationResponse response = restTemplate.exchange(url, HttpMethod.POST, entity, PaystackInitializationResponse.class).getBody();
            if (response != null && response.isStatus()) {
                return PaymentResponse.builder()
                        .authorizationUrl(response.getData().getAuthorizationUrl())
                        .accessCode(response.getData().getAccessCode())
                        .reference(response.getData().getReference())
                        .paymentStatus(PaymentStatus.PROCESSING)
                        .build();
            } else {
                throw new BusinessException("Payment initialization failed: " + (response != null ? response.getMessage() : "No response"));
            }
        } catch (Exception e) {
            throw new BusinessException("Payment initialization failed: " + e.getMessage());
        }
    }

    @Override
    public VerificationResponse verifyPayment(String reference) {
        String url = paystackApiBaseUrl + "/transaction/verify/" + reference;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            PaystackVerificationResponse response = restTemplate.exchange(url, HttpMethod.GET, entity, PaystackVerificationResponse.class).getBody();
            if (response != null && response.isStatus()) {
                return VerificationResponse.builder()
                        .verified(true)
                        .message(response.getMessage())
                        .reference(reference)
                        .paymentStatus(PaymentStatus.PAID)
                        .reconciled(true)
                        .build();
            } else {
                return VerificationResponse.builder()
                        .verified(false)
                        .message(response != null ? response.getMessage() : "No response")
                        .reference(reference)
                        .paymentStatus(PaymentStatus.FAILED)
                        .reconciled(false)
                        .build();
            }
        } catch (Exception e) {
            return VerificationResponse.builder()
                    .verified(false)
                    .message("Payment verification failed: " + e.getMessage())
                    .reference(reference)
                    .paymentStatus(PaymentStatus.FAILED)
                    .reconciled(false)
                    .build();
        }
    }

    @Override
    public String getName() {
        return PaymentProcessorNames.PAYSTACK.name();
    };

    private void verifySignature(String signature, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(paystackSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String mySignature = bytesToHex(hash);

            if (signature == null || !mySignature.equals(signature)) {
                throw new BusinessException("Invalid Paystack signature");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ApplicationException("Error verifying Paystack signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public void handleWebHook(String signature, String payload) {
        try {
            verifySignature(signature, payload);
            PaystackWebhookEvent event = objectMapper.readValue(payload, PaystackWebhookEvent.class);

            if ("charge.success".equals(event.getEvent())) {
                String reference = event.getData().getReference();
                managePayments.handlePaymentNotification(reference, PaymentStatus.PAID, event.getEvent());
            } else if ("charge.failed".equals(event.getEvent())) {
                managePayments.handlePaymentNotification(event.getData().getReference(), PaymentStatus.FAILED, event.getEvent());
            } else if ("refund.processed".equals(event.getEvent())) {
                managePayments.handlePaymentNotification(event.getData().getReference(), PaymentStatus.REVERSED, event.getEvent());
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing webhook event", e);
        }
    }
}
