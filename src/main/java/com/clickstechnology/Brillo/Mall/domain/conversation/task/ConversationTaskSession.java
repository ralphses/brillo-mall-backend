package com.clickstechnology.Brillo.Mall.domain.conversation.task;

import com.clickstechnology.Brillo.Mall.application.enums.TaskDecisionSource;
import com.clickstechnology.Brillo.Mall.application.enums.TaskSessionStatus;
import com.clickstechnology.Brillo.Mall.domain.conversation.Conversation;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("record_status <> 'DELETED'")
@Table(name = "conversation_task_sessions")
public class ConversationTaskSession extends JpaAuditor implements Serializable {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false, unique = true)
    private Conversation conversation;

    @Column(name = "business_id", nullable = false, length = 36)
    private String businessId;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "whatsapp_conversation_id", nullable = false, length = 100)
    private String whatsappConversationId;

    @Column(name = "current_intent", length = 100)
    private String currentIntent;

    @Column(name = "current_task_key", length = 100)
    private String currentTaskKey;

    @Column(name = "current_state_key", length = 100)
    private String currentStateKey;

    @Column(name = "paused_task_key", length = 100)
    private String pausedTaskKey;

    @Column(name = "paused_state_key", length = 100)
    private String pausedStateKey;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false, length = 30)
    private TaskSessionStatus taskStatus = TaskSessionStatus.ACTIVE;

    @Builder.Default
    @Column(name = "confidence", nullable = false)
    private Double confidence = 0.0d;

    @Column(name = "slots_json", columnDefinition = "TEXT")
    private String slotsJson;

    @Column(name = "last_normalized_input", columnDefinition = "TEXT")
    private String lastNormalizedInput;

    @Column(name = "last_ai_reason", columnDefinition = "TEXT")
    private String lastAiReason;

    @Column(name = "last_ai_model", length = 100)
    private String lastAiModel;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "last_decision_source", length = 30)
    private TaskDecisionSource lastDecisionSource = TaskDecisionSource.RULE;

    @Column(name = "last_reply_type", length = 50)
    private String lastReplyType;

    @Column(name = "last_route_to", length = 100)
    private String lastRouteTo;

    @Column(name = "last_turn_at")
    private Instant lastTurnAt;

    @Builder.Default
    @Column(name = "transition_count", nullable = false)
    private Integer transitionCount = 0;

    @Builder.Default
    @Column(name = "human_takeover", nullable = false)
    private Boolean humanTakeover = Boolean.FALSE;
}
