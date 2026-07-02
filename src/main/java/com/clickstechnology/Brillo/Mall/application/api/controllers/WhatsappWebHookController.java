package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.WhatsappService;
import com.fasterxml.jackson.databind.JsonNode;
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
public class WhatsappWebHookController {
    private final WhatsappService whatsappService;

    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge
    ) {
        return ResponseEntity.ok(whatsappService.verifyWebhook(mode, verifyToken, challenge));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody JsonNode payload) {
        whatsappService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
