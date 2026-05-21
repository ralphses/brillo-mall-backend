package com.clickstechnology.Brillo.Mall.application.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhatsappResponse {

    @JsonProperty("messaging_product")
    private String messagingProduct;
    private List<ResponseContact> contacts;
    private List<ResponseMessage> messages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseContact {
        private String input;

        @JsonProperty("wa_id")
        private String waId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseMessage {
        private String id;
    }
}
