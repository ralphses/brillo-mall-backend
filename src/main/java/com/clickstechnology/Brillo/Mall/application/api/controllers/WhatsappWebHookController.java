package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.WhatsappService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/whatsapp")
@Tag(name = "WhatsApp Webhook", description = "WhatsApp webhook verification and ingestion APIs")
public class WhatsappWebHookController {
    private final WhatsappService whatsappService;

    @GetMapping("/webhook")
    @Operation(summary = "Verify WhatsApp webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge
    ) {
        return ResponseEntity.ok(whatsappService.verifyWebhook(mode, verifyToken, challenge));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Receive WhatsApp webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody JsonNode payload) {
        whatsappService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
