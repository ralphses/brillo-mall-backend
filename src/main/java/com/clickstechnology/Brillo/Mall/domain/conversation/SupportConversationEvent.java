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
@Table(name = "support_conversation_events")
public class SupportConversationEvent extends JpaAuditor implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "actor_user_id", nullable = false, length = 36)
    private String actorUserId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "from_assigned_support_user_id", length = 36)
    private String fromAssignedSupportUserId;

    @Column(name = "to_assigned_support_user_id", length = 36)
    private String toAssignedSupportUserId;

    @Column(name = "from_active_business_id", length = 36)
    private String fromActiveBusinessId;

    @Column(name = "to_active_business_id", length = 36)
    private String toActiveBusinessId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
