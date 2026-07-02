package com.clickstechnology.Brillo.Mall.application.features.runtime;

import com.clickstechnology.Brillo.Mall.application.enums.TaskDecisionSource;
import com.clickstechnology.Brillo.Mall.application.enums.TaskSessionStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.domain.conversation.Conversation;
import com.clickstechnology.Brillo.Mall.domain.conversation.task.ConversationTaskEvent;
import com.clickstechnology.Brillo.Mall.domain.conversation.task.ConversationTaskEventRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.task.ConversationTaskSession;
import com.clickstechnology.Brillo.Mall.domain.conversation.task.ConversationTaskSessionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskSessionService {

    private final com.clickstechnology.Brillo.Mall.domain.conversation.ConversationRepository conversationRepository;
    private final ConversationTaskSessionRepository taskSessionRepository;
    private final ConversationTaskEventRepository taskEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ConversationTaskSession getOrCreate(com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto conversation,
                                               String businessId,
                                               String customerId) {
        return taskSessionRepository.findByConversation_Reference(conversation.getReference())
                .orElseGet(() -> taskSessionRepository.save(ConversationTaskSession.builder()
                        .conversation(conversationRepository.findByReference(conversation.getReference())
                                .orElseThrow(() -> new IllegalStateException("Conversation not found for task session")))
                        .businessId(businessId)
                        .customerId(customerId)
                        .whatsappConversationId(conversation.getWhatsappConversationId())
                        .taskStatus(TaskSessionStatus.ACTIVE)
                        .confidence(0.0d)
                        .transitionCount(0)
                        .humanTakeover(Boolean.FALSE)
                        .build()));
    }

    @Transactional
    public void persistTransition(
            ConversationTaskSession session,
            TaskTurnResult result,
            String inputText,
            String normalizedInput,
            Map<String, Object> slots,
            String rawPayload
    ) {
        session.setCurrentIntent(result.intentKey());
        session.setCurrentTaskKey(result.taskKey());
        session.setCurrentStateKey(result.stateKey());
        session.setConfidence(result.confidence());
        session.setSlotsJson(serialize(slots));
        session.setLastNormalizedInput(normalizedInput);
        session.setLastAiReason(result.aiReason());
        session.setLastAiModel(result.aiModel());
        session.setLastDecisionSource(result.decisionSource());
        session.setLastReplyType(result.presentationType() != null ? result.presentationType().getValue() : null);
        session.setLastRouteTo(result.routeTo());
        session.setLastTurnAt(Instant.now());
        session.setTaskStatus(result.taskSessionStatus());
        session.setHumanTakeover(result.humanTakeover());
        session.setTransitionCount((session.getTransitionCount() == null ? 0 : session.getTransitionCount()) + 1);
        taskSessionRepository.save(session);

        ConversationTaskEvent event = ConversationTaskEvent.builder()
                .session(session)
                .conversationId(session.getConversation().getReference())
                .businessId(session.getBusinessId())
                .customerId(session.getCustomerId())
                .whatsappConversationId(session.getWhatsappConversationId())
                .inputText(inputText)
                .normalizedInput(normalizedInput)
                .intentKey(result.intentKey())
                .routeToKey(result.routeTo())
                .taskKey(result.taskKey())
                .stateKey(result.stateKey())
                .decisionSource(result.decisionSource())
                .confidence(result.confidence())
                .slotSnapshot(serialize(slots))
                .replyType(result.presentationType())
                .replyText(result.replyText())
                .aiModel(result.aiModel())
                .aiReason(result.aiReason())
                .eventType(result.taskSessionStatus().name())
                .rawPayload(rawPayload)
                .build();
        taskEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> readSlots(ConversationTaskSession session) {
        if (session.getSlotsJson() == null || session.getSlotsJson().isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(session.getSlotsJson(), new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    @Transactional
    public void storeSlots(ConversationTaskSession session, Map<String, Object> slots) {
        session.setSlotsJson(serialize(slots));
        taskSessionRepository.save(session);
    }

    private String serialize(Map<String, Object> slots) {
        try {
            return objectMapper.writeValueAsString(slots == null ? Map.of() : slots);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
