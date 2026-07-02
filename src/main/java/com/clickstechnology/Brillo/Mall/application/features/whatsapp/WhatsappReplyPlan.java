package com.clickstechnology.Brillo.Mall.application.features.whatsapp;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;

record WhatsappReplyPlan(
        String content,
        WhatsappMessageType transportType,
        Object payload,
        String intent,
        String activeTaskKey,
        ConversationStatus status,
        boolean humanTakeover
) {
}
