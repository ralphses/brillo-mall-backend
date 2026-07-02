package com.clickstechnology.Brillo.Mall.domain.conversation.task;

import com.clickstechnology.Brillo.Mall.application.enums.TaskDecisionSource;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "conversation_task_events")
public class ConversationTaskEvent extends JpaAuditor implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationTaskSession session;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @Column(name = "business_id", nullable = false)
    private String businessId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "whatsapp_conversation_id", nullable = false, length = 100)
    private String whatsappConversationId;

    @Column(name = "input_text", columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "normalized_input", columnDefinition = "TEXT")
    private String normalizedInput;

    @Column(name = "intent_key", length = 100)
    private String intentKey;

    @Column(name = "route_to_key", length = 100)
    private String routeToKey;

    @Column(name = "task_key", length = 100)
    private String taskKey;

    @Column(name = "state_key", length = 100)
    private String stateKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_source", length = 30)
    private TaskDecisionSource decisionSource;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "slot_snapshot", columnDefinition = "TEXT")
    private String slotSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "reply_type", length = 50)
    private WhatsappMessageType replyType;

    @Column(name = "reply_text", columnDefinition = "TEXT")
    private String replyText;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(name = "ai_reason", columnDefinition = "TEXT")
    private String aiReason;

    @Column(name = "event_type", length = 30)
    private String eventType;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;
}
