package com.clickstechnology.Brillo.Mall.application.features.whatsapp;

import java.time.Instant;

record WhatsappInboundEvent(
        String senderPhone,
        String senderName,
        String businessPhoneNumber,
        String businessPhoneNumberId,
        String whatsappMessageId,
        String whatsappConversationId,
        String sourceEventId,
        String content,
        String interactiveReplyId,
        String interactiveReplyTitle,
        String inboundType,
        String contextMessageId,
        Instant occurredAt,
        String metadata
) {
}
