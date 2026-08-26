package com.clickstechnology.Brillo.Mall.domain.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByReference(String reference);
    Optional<Conversation> findByWhatsappConversationIdAndChannelKeyAndConversationMode(
            String whatsappConversationId,
            String channelKey,
            ConversationMode conversationMode);
    Optional<Conversation> findTopByWhatsappConversationIdAndChannelKeyAndConversationModeInOrderByLastInteractionAtDesc(
            String whatsappConversationId,
            String channelKey,
            List<ConversationMode> conversationModes);
    List<Conversation> findAllByWhatsappConversationId(String whatsappConversationId);
}
