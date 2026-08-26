package com.clickstechnology.Brillo.Mall.domain.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
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
import java.util.List;
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
        Conversation conversation = resolveConversation(request)
                .orElseGet(Conversation::new);

        if (conversation.getId() == null) {
            conversation.setReference(request.getReference());
            conversation.setBusinessId(request.getBusinessId());
            conversation.setCustomerId(request.getCustomerId());
            conversation.setWhatsappConversationId(request.getWhatsappConversationId());
        }

        conversation.setBusinessId(request.getBusinessId());
        conversation.setCustomerId(request.getCustomerId());
        conversation.setWhatsappBusinessNumber(request.getWhatsappBusinessNumber());
        conversation.setChannelKey(request.getChannelKey());
        conversation.setConversationMode(request.getConversationMode());
        conversation.setEntryBusinessId(request.getEntryBusinessId());
        conversation.setActiveBusinessId(request.getActiveBusinessId());
        conversation.setEntrySlug(request.getEntrySlug());
        conversation.setMarketplaceMode(Boolean.TRUE.equals(request.getMarketplaceMode()));
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
    public Optional<ConversationDto> findByContext(String whatsappId, String channelKey, ConversationMode conversationMode) {
        return conversationRepository.findByWhatsappConversationIdAndChannelKeyAndConversationMode(whatsappId, channelKey, conversationMode)
                .map(this::mapToDto);
    }

    @Override
    public Optional<ConversationDto> findLatestSharedConversation(String whatsappId, String channelKey) {
        return conversationRepository.findTopByWhatsappConversationIdAndChannelKeyAndConversationModeInOrderByLastInteractionAtDesc(
                        whatsappId,
                        channelKey,
                        List.of(ConversationMode.SHARED_MARKETPLACE, ConversationMode.SHARED_BUSINESS))
                .map(this::mapToDto);
    }

    @Override
    public List<ConversationDto> findAllByWhatsappId(String whatsappId) {
        return conversationRepository.findAllByWhatsappConversationId(whatsappId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private Optional<Conversation> resolveConversation(ConversationUpsertRequest request) {
        if (request.getReference() != null && !request.getReference().isBlank()) {
            Optional<Conversation> byReference = conversationRepository.findByReference(request.getReference());
            if (byReference.isPresent()) {
                return byReference;
            }
        }

        if (request.getWhatsappConversationId() == null || request.getWhatsappConversationId().isBlank()
                || request.getChannelKey() == null || request.getChannelKey().isBlank()
                || request.getConversationMode() == null) {
            return Optional.empty();
        }

        return conversationRepository.findByWhatsappConversationIdAndChannelKeyAndConversationMode(
                request.getWhatsappConversationId(),
                request.getChannelKey(),
                request.getConversationMode()
        );
    }

    private ConversationDto mapToDto(Conversation conversation) {
        return ConversationDto.builder()
                .id(conversation.getId() != null ? conversation.getId().toString() : null)
                .reference(conversation.getReference())
                .businessId(conversation.getBusinessId())
                .customerId(conversation.getCustomerId())
                .whatsappConversationId(conversation.getWhatsappConversationId())
                .whatsappBusinessNumber(conversation.getWhatsappBusinessNumber())
                .channelKey(conversation.getChannelKey())
                .conversationMode(conversation.getConversationMode())
                .entryBusinessId(conversation.getEntryBusinessId())
                .activeBusinessId(conversation.getActiveBusinessId())
                .entrySlug(conversation.getEntrySlug())
                .marketplaceMode(conversation.getMarketplaceMode())
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
