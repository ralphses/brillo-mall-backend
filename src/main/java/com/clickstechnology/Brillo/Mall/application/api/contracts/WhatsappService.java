package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.fasterxml.jackson.databind.JsonNode;

public interface WhatsappService {
    String verifyWebhook(String mode, String verifyToken, String challenge);

    void handleWebhook(JsonNode payload);

    void processWebhookPayload(JsonNode payload);
}
