package com.clickstechnology.Brillo.Mall.application.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    PAY_ON_DELIVERY("Pay on delivery"),
    ONLINE("online");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}