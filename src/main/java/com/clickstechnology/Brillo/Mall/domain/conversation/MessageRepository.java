package com.clickstechnology.Brillo.Mall.domain.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findByReference(String reference);
    boolean existsByWhatsappMessageId(String whatsappMessageId);
    Optional<Message> findByWhatsappMessageId(String whatsappMessageId);
}
