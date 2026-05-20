package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageDto;

import java.util.Optional;

public interface ConversationService {
    ConversationDto getOrCreateConversation(String businessId, String customerId, String whatsappConversationId);
    MessageDto addMessage(String conversationReference, String content, String type, String intent);
    Optional<ConversationDto> findByWhatsappId(String whatsappId);
}
