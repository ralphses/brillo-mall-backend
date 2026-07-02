package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentRequest;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentResponse;
import com.clickstechnology.Brillo.Mall.application.dto.payments.VerificationResponse;

public interface PaymentProcessor {

    PaymentResponse initializePayment(PaymentRequest request);
    VerificationResponse verifyPayment(String reference);
    String getName();

    void handleWebHook(String signature, String payload);
}
