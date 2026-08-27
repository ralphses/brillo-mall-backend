package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentInitializationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentResponse;
import com.clickstechnology.Brillo.Mall.application.dto.payments.VerificationResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.features.payments.ManagePayments;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/payments")
@Tag(name = "Payments", description = "Payment initialization, verification, and webhook APIs")
public class PaymentController {

    private final ManagePayments managePayments;

    @PostMapping("/initialize")
    @Operation(summary = "Initialize payment")
    public ResponseWrapper<PaymentResponse> initializePayment(@Valid @RequestBody PaymentInitializationRequest paymentRequest, HttpServletRequest httpServletRequest) {
        return success(managePayments.initializePayment(paymentRequest, httpServletRequest));
    }

    @PostMapping("/webhooks/{processor}")
    @Operation(summary = "Handle payment webhook")
    public ResponseEntity<Void> handleWebHook(
            @PathVariable String processor,
            @RequestHeader(value = "X-Paystack-Signature", required = false) String signature,
            @RequestBody final String payload,
            HttpServletRequest httpServletRequest) {
        managePayments.handleWebHook(processor, signature, payload, httpServletRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify/{reference}")
    @Operation(summary = "Verify payment")
    public ResponseWrapper<VerificationResponse> verifyPayment(@PathVariable String reference) {
        return success(managePayments.verifyPayment(reference));
    }

}
