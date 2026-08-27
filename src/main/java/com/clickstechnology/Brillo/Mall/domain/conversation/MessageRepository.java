package com.clickstechnology.Brillo.Mall.domain.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findByReference(String reference);
    boolean existsByWhatsappMessageId(String whatsappMessageId);
    Optional<Message> findByWhatsappMessageId(String whatsappMessageId);
    List<Message> findTop20ByConversation_ReferenceOrderByCreatedAtDesc(String conversationReference);
}
