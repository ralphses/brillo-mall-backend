package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.WhatsappService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/whatsapp")
public class WhatsappWebHookController {
    private final WhatsappService whatsappService;
}
