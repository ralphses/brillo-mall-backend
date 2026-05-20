package com.clickstechnology.Brillo.Mall.domain.conversation;

import com.clickstechnology.Brillo.Mall.application.api.contracts.ConversationService;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageDto;
import com.clickstechnology.Brillo.Mall.application.enums.MessageType;
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
    public ConversationDto getOrCreateConversation(String businessId, String customerId, String whatsappConversationId) {
        return conversationRepository.findByWhatsappConversationId(whatsappConversationId)
                .map(this::mapToDto)
                .orElseGet(() -> {
                    Conversation conversation = Conversation.builder()
                            .businessId(businessId)
                            .customerId(customerId)
                            .whatsappConversationId(whatsappConversationId)
                            .lastInteractionAt(Instant.now())
                            .build();
                    return mapToDto(conversationRepository.save(conversation));
                });
    }

    @Override
    @Transactional
    public MessageDto addMessage(String conversationReference, String content, String type, String intent) {
        Conversation conversation = conversationRepository.findByReference(conversationReference)
                .orElseThrow(() -> new BusinessException("Conversation not found"));
        
        Message message = Message.builder()
                .conversation(conversation)
                .content(content)
                .messageType(MessageType.valueOf(type.toUpperCase()))
                .intent(intent)
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
                .lastInteractionAt(conversation.getLastInteractionAt())
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
                .createdAt(message.getCreatedAt())
                .build();
    }
}
