package com.clickstechnology.Brillo.Mall.domain.conversation.flow;

import com.clickstechnology.Brillo.Mall.application.enums.FlowSessionStatus;
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
@Table(name = "conversation_flow_sessions")
public class ConversationFlowSession extends JpaAuditor implements Serializable {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false, unique = true)
    private Conversation conversation;

    @Column(name = "business_id", nullable = false, length = 36)
    private String businessId;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "whatsapp_conversation_id", nullable = false, length = 100)
    private String whatsappConversationId;

    @Column(name = "flow_id", length = 100)
    private String flowId;

    @Column(name = "flow_name", length = 100)
    private String flowName;

    @Column(name = "flow_token", length = 120)
    private String flowToken;

    @Column(name = "flow_cta", length = 100)
    private String flowCta;

    @Column(name = "flow_mode", length = 30)
    private String flowMode;

    @Column(name = "flow_action", length = 100)
    private String flowAction;

    @Column(name = "flow_message_version", length = 30)
    private String flowMessageVersion;

    @Column(name = "launch_task_key", length = 100)
    private String launchTaskKey;

    @Column(name = "launch_state_key", length = 100)
    private String launchStateKey;

    @Column(name = "launch_intent_key", length = 100)
    private String launchIntentKey;

    @Column(name = "launch_presentation_type", length = 50)
    private String launchPresentationType;

    @Column(name = "correlation_reference", length = 100)
    private String correlationReference;

    @Column(name = "launch_payload", columnDefinition = "TEXT")
    private String launchPayload;

    @Column(name = "submission_payload", columnDefinition = "TEXT")
    private String submissionPayload;

    @Column(name = "last_inbound_event_id", length = 100)
    private String lastInboundEventId;

    @Column(name = "last_outbound_message_id", length = 100)
    private String lastOutboundMessageId;

    @Column(name = "last_route_key", length = 100)
    private String lastRouteKey;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "flow_status", nullable = false, length = 30)
    private FlowSessionStatus flowStatus = FlowSessionStatus.LAUNCHED;

    @Column(name = "launched_at")
    private Instant launchedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
