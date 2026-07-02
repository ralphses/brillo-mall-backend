package com.clickstechnology.Brillo.Mall.domain.conversation;

import com.clickstechnology.Brillo.Mall.application.api.contracts.ConversationService;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationUpsertRequest;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageCreateRequest;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageDto;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public ConversationDto upsertConversation(ConversationUpsertRequest request) {
        Conversation conversation = conversationRepository.findByWhatsappConversationId(request.getWhatsappConversationId())
                .orElseGet(Conversation::new);

        if (conversation.getId() == null) {
            conversation.setBusinessId(request.getBusinessId());
            conversation.setCustomerId(request.getCustomerId());
            conversation.setWhatsappConversationId(request.getWhatsappConversationId());
        }

        conversation.setBusinessId(request.getBusinessId());
        conversation.setCustomerId(request.getCustomerId());
        conversation.setWhatsappBusinessNumber(request.getWhatsappBusinessNumber());
        if (request.getStatus() != null) {
            conversation.setStatus(request.getStatus());
        }
        conversation.setLastIntent(request.getLastIntent());
        conversation.setActiveTaskKey(request.getActiveTaskKey());
        conversation.setHumanTakeover(Boolean.TRUE.equals(request.getHumanTakeover()));
        conversation.setLastInteractionAt(request.getLastInteractionAt() != null ? request.getLastInteractionAt() : Instant.now());
        conversation.setSessionExpiresAt(request.getSessionExpiresAt());

        return mapToDto(conversationRepository.save(conversation));
    }

    @Override
    @Transactional
    public MessageDto addMessage(MessageCreateRequest request) {
        Conversation conversation = conversationRepository.findByReference(request.getConversationReference())
                .orElseThrow(() -> new BusinessException("Conversation not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .intent(request.getIntent())
                .whatsappMessageId(request.getWhatsappMessageId())
                .transportType(request.getTransportType())
                .sourceEventId(request.getSourceEventId())
                .metadata(request.getMetadata())
                .build();

        conversation.setLastInteractionAt(Instant.now());
        conversationRepository.save(conversation);

        return mapToDto(messageRepository.save(message));
    }

    @Override
    public Optional<ConversationDto> findByWhatsappId(String whatsappId) {
        return conversationRepository.findByWhatsappConversationId(whatsappId)
                .map(this::mapToDto);
    }

    private ConversationDto mapToDto(Conversation conversation) {
        return ConversationDto.builder()
                .id(conversation.getId() != null ? conversation.getId().toString() : null)
                .reference(conversation.getReference())
                .businessId(conversation.getBusinessId())
                .customerId(conversation.getCustomerId())
                .whatsappConversationId(conversation.getWhatsappConversationId())
                .whatsappBusinessNumber(conversation.getWhatsappBusinessNumber())
                .status(conversation.getStatus())
                .lastIntent(conversation.getLastIntent())
                .activeTaskKey(conversation.getActiveTaskKey())
                .humanTakeover(conversation.getHumanTakeover())
                .lastInteractionAt(conversation.getLastInteractionAt())
                .sessionExpiresAt(conversation.getSessionExpiresAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messages(conversation.getMessages() != null ? 
                    conversation.getMessages().stream().map(this::mapToDto).collect(Collectors.toList()) : null)
                .build();
    }

    private MessageDto mapToDto(Message message) {
        return MessageDto.builder()
                .id(message.getId() != null ? message.getId().toString() : null)
                .reference(message.getReference())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .intent(message.getIntent())
                .whatsappMessageId(message.getWhatsappMessageId())
                .transportType(message.getTransportType())
                .sourceEventId(message.getSourceEventId())
                .metadata(message.getMetadata())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
