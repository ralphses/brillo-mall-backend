package com.clickstechnology.Brillo.Mall.application.dto.whatsapp;

import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class WhatsAppMessageRequest {

    @Builder.Default
    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";

    @Builder.Default
    @JsonProperty("recipient_type")
    private String recipientType = "individual";

    private String to;
    private WhatsappMessageType type;
}
