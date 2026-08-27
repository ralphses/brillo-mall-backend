package com.clickstechnology.Brillo.Mall.domain.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportConversationEventRepository extends JpaRepository<SupportConversationEvent, Long> {
    List<SupportConversationEvent> findTop50ByConversation_ReferenceOrderByCreatedAtDesc(String conversationReference);
}
