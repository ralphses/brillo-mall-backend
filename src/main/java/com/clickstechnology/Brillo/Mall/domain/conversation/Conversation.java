package com.clickstechnology.Brillo.Mall.domain.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "conversations",
        uniqueConstraints = {
                @jakarta.persistence.UniqueConstraint(name = "uk_conversations_whatsapp_conversation_id", columnNames = {"whatsapp_conversation_id"})
        }
)
public class Conversation extends JpaAuditor implements Serializable {

    @Column(name = "business_id", nullable = false)
    private String businessId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "whatsapp_conversation_id")
    private String whatsappConversationId;

    @Column(name = "whatsapp_business_number", length = 30)
    private String whatsappBusinessNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "last_intent", length = 100)
    private String lastIntent;

    @Column(name = "active_task_key", length = 100)
    private String activeTaskKey;

    @Builder.Default
    @Column(name = "human_takeover", nullable = false)
    private Boolean humanTakeover = Boolean.FALSE;

    @Column(name = "last_interaction_at")
    private Instant lastInteractionAt;

    @Column(name = "session_expires_at")
    private Instant sessionExpiresAt;

    @Builder.Default
    @OneToMany(mappedBy = "conversation", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();
}
