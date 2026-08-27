package com.clickstechnology.Brillo.Mall.domain.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportConversationNoteRepository extends JpaRepository<SupportConversationNote, Long> {
    List<SupportConversationNote> findTop20ByConversation_ReferenceOrderByCreatedAtDesc(String conversationReference);
}
