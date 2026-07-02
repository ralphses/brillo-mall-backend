package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationUpsertRequest;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageCreateRequest;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageDto;

import java.util.Optional;

public interface ConversationService {
    ConversationDto upsertConversation(ConversationUpsertRequest request);
    MessageDto addMessage(MessageCreateRequest request);
    Optional<ConversationDto> findByWhatsappId(String whatsappId);
}
