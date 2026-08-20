package com.clickstechnology.Brillo.Mall.infrastructure.external;

import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentProcessor;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentProcessorResolver;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.external.dtos.PaymentProcessorNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
class DefaultPaymentProcessorResolver implements PaymentProcessorResolver {

    public static final Map<PaymentProcessorNames, PaymentProcessor> paymentProcessorMap = new HashMap<>();

    @Override
    public PaymentProcessor resolve(String processorName) {
        try {
            return paymentProcessorMap.get(PaymentProcessorNames.valueOf(processorName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid payment processor name: " + processorName);
        }
    }
}
