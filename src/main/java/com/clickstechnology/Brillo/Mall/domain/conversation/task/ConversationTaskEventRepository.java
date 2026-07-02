package com.clickstechnology.Brillo.Mall.domain.conversation.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationTaskEventRepository extends JpaRepository<ConversationTaskEvent, Long> {
}
