package com.clickstechnology.Brillo.Mall.infrastructure.external;

import com.clickstechnology.Brillo.Mall.application.features.payments.ManagePayments;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaystackPaymentProcessorTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ManagePayments managePayments;

    private PaystackPaymentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PaystackPaymentProcessor(restTemplate, new ObjectMapper(), managePayments);
        ReflectionTestUtils.setField(processor, "paystackSecretKey", "test-secret");
        ReflectionTestUtils.setField(processor, "paystackApiBaseUrl", "https://paystack.test");
    }

    @Test
    void handleWebHook_shouldRejectInvalidSignature() {
        String payload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"ref-123\"}}";

        assertThatThrownBy(() -> processor.handleWebHook(null, payload))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid Paystack signature");
    }

    @Test
    void handleWebHook_shouldReconcileSuccessfulCharge() {
        String payload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"ref-123\"}}";
        String signature = generateSignature("test-secret", payload);
        doNothing().when(managePayments).handlePaymentNotification(anyString(), any(), anyString());

        processor.handleWebHook(signature, payload);

        verify(managePayments).handlePaymentNotification("ref-123", com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus.PAID, "charge.success");
    }

    private String generateSignature(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte value : hash) {
                sb.append(String.format("%02x", value));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
