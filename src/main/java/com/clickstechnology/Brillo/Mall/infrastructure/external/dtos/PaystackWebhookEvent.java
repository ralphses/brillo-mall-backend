package com.clickstechnology.Brillo.Mall.infrastructure.external.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaystackWebhookEvent {

    private String event;
    private Data data;

    @lombok.Data
    public static class Data {
        private String reference;
        private String status;
        private BigDecimal amount;
        private String currency;
        @JsonProperty("paid_at")
        private String paidAt;
        private Map<String, Object> customer;
        private Map<String, Object> authorization;
    }
}