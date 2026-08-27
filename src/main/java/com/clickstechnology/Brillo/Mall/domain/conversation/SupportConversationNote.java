package com.clickstechnology.Brillo.Mall.domain.conversation;

import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("record_status <> 'DELETED'")
@Table(name = "support_conversation_notes")
public class SupportConversationNote extends JpaAuditor implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "actor_user_id", nullable = false, length = 36)
    private String actorUserId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}
