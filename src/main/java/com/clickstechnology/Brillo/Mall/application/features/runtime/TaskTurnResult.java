package com.clickstechnology.Brillo.Mall.application.features.runtime;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.TaskDecisionSource;
import com.clickstechnology.Brillo.Mall.application.enums.TaskSessionStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;

import java.util.List;
import java.util.Map;

public record TaskTurnResult(
        String intentKey,
        String routeTo,
        String taskKey,
        String stateKey,
        double confidence,
        TaskDecisionSource decisionSource,
        TaskSessionStatus taskSessionStatus,
        ConversationStatus conversationStatus,
        boolean humanTakeover,
        String replyText,
        WhatsappMessageType presentationType,
        WhatsAppMessageRequest outboundMessage,
        Map<String, Object> slots,
        List<String> missingSlots,
        String aiReason,
        String aiModel
) {
}
