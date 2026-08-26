package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationUpsertRequest;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageCreateRequest;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageDto;

import java.util.List;
import java.util.Optional;

public interface ConversationService {
    ConversationDto upsertConversation(ConversationUpsertRequest request);
    MessageDto addMessage(MessageCreateRequest request);
    Optional<ConversationDto> findByContext(String whatsappId, String channelKey, ConversationMode conversationMode);
    Optional<ConversationDto> findLatestSharedConversation(String whatsappId, String channelKey);
    List<ConversationDto> findAllByWhatsappId(String whatsappId);
}
