package com.clickstechnology.Brillo.Mall.domain.conversation.flow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationFlowSessionRepository extends JpaRepository<ConversationFlowSession, Long> {
    Optional<ConversationFlowSession> findByReference(String reference);

    Optional<ConversationFlowSession> findByConversation_Reference(String conversationReference);

    Optional<ConversationFlowSession> findByWhatsappConversationId(String whatsappConversationId);
}
