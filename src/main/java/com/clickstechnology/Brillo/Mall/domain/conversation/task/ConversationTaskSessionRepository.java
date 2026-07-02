package com.clickstechnology.Brillo.Mall.domain.conversation.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationTaskSessionRepository extends JpaRepository<ConversationTaskSession, Long> {
    Optional<ConversationTaskSession> findByConversation_Reference(String conversationReference);
    Optional<ConversationTaskSession> findByWhatsappConversationId(String whatsappConversationId);
}
