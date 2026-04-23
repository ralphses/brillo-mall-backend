package com.clickstechnology.Brillo.Mall.application.api.contracts;

public interface PaymentProcessorResolver {
    PaymentProcessor resolve(String processor);
}
