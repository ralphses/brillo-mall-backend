package com.clickstechnology.Brillo.Mall.infrastructure.external.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaystackInitializationResponse {
    private boolean status;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        @JsonProperty("authorization_url")
        private String authorizationUrl;
        @JsonProperty("access_code")
        private String accessCode;
        private String reference;
    }
}
