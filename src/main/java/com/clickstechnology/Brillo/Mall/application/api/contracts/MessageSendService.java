package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsappResponse;

@FunctionalInterface
public interface MessageSendService {
    WhatsappResponse sendMessage(WhatsAppMessageRequest whatsAppMessageRequest);
}
