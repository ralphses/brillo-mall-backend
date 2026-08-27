package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.BusinessConversationDetailDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.BusinessConversationSummaryDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.ConversationUpsertRequest;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.UpdateConversationActiveBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationBusinessAttribution;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.MessageType;
import com.clickstechnology.Brillo.Mall.application.enums.TaskSessionStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.features.notifications.NotificationEventPublisher;
import com.clickstechnology.Brillo.Mall.application.features.runtime.TaskSessionService;
import com.clickstechnology.Brillo.Mall.domain.conversation.Conversation;
import com.clickstechnology.Brillo.Mall.domain.conversation.ConversationRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.Message;
import com.clickstechnology.Brillo.Mall.domain.conversation.MessageRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageBusinessConversations {

    private final TenantContextResolver tenantContextResolver;
    private final BusinessService businessService;
    private final CustomerService customerService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final TaskSessionService taskSessionService;
    private final NotificationEventPublisher notificationEventPublisher;

    @Transactional(readOnly = true)
    public PaginatedResponse<BusinessConversationSummaryDto> list(
            String businessId,
            Boolean humanTakeover,
            Boolean reopenedOnly,
            Integer page,
            Integer pageSize,
            HttpServletRequest httpServletRequest) {
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
        List<Conversation> conversations = conversationRepository.findAllVisibleByBusinessId(businessId).stream()
                .filter(conversation -> humanTakeover == null || conversation.getHumanTakeover().equals(humanTakeover))
                .filter(conversation -> reopenedOnly == null || !reopenedOnly || conversation.getLastReopenedAt() != null)
                .sorted(Comparator.comparing(Conversation::getLastInteractionAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Conversation::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        int fromIndex = Math.min((safePage - 1) * safePageSize, conversations.size());
        int toIndex = Math.min(fromIndex + safePageSize, conversations.size());
        List<BusinessConversationSummaryDto> items = conversations.subList(fromIndex, toIndex).stream()
                .map(conversation -> toSummary(conversation, businessId))
                .toList();

        int totalPages = safePageSize == 0 ? 1 : (int) Math.ceil((double) conversations.size() / safePageSize);
        return PaginatedResponse.<BusinessConversationSummaryDto>builder()
                .page(safePage)
                .perPage(safePageSize)
                .total(conversations.size())
                .totalPages(totalPages)
                .hasNext(toIndex < conversations.size())
                .hasPrevious(safePage > 1)
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public BusinessConversationDetailDto getDetail(String businessId, String conversationReference, HttpServletRequest httpServletRequest) {
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
        Conversation conversation = findVisibleConversation(businessId, conversationReference);
        List<MessageDto> recentMessages = messageRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc(conversationReference)
                .stream()
                .map(this::toMessageDto)
                .sorted(Comparator.comparing(MessageDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        BusinessConversationSummaryDto summary = toSummary(conversation, businessId);
        return BusinessConversationDetailDto.builder()
                .reference(summary.getReference())
                .customerId(summary.getCustomerId())
                .whatsappConversationId(summary.getWhatsappConversationId())
                .channelKey(summary.getChannelKey())
                .conversationMode(summary.getConversationMode())
                .status(summary.getStatus())
                .entryBusinessId(summary.getEntryBusinessId())
                .activeBusinessId(summary.getActiveBusinessId())
                .entrySlug(summary.getEntrySlug())
                .marketplaceMode(summary.getMarketplaceMode())
                .humanTakeover(summary.getHumanTakeover())
                .lastIntent(summary.getLastIntent())
                .activeTaskKey(summary.getActiveTaskKey())
                .lastInteractionAt(summary.getLastInteractionAt())
                .sessionExpiresAt(summary.getSessionExpiresAt())
                .reopenCount(summary.getReopenCount())
                .lastReopenedAt(summary.getLastReopenedAt())
                .lastSessionEvent(summary.getLastSessionEvent())
                .lastSessionEventAt(summary.getLastSessionEventAt())
                .lastInboundMessage(summary.getLastInboundMessage())
                .lastOutboundMessage(summary.getLastOutboundMessage())
                .lastInboundAt(summary.getLastInboundAt())
                .lastOutboundAt(summary.getLastOutboundAt())
                .attribution(summary.getAttribution())
                .recentMessages(recentMessages)
                .build();
    }

    @Transactional
    public BusinessConversationDetailDto requestTakeover(String businessId, String conversationReference, HttpServletRequest httpServletRequest) {
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
        Conversation conversation = findVisibleConversation(businessId, conversationReference);
        conversation.setHumanTakeover(Boolean.TRUE);
        conversation.setStatus(ConversationStatus.HUMAN_TAKEOVER);
        conversation.setLastSessionEvent("HUMAN_TAKEOVER_REQUESTED");
        conversation.setLastSessionEventAt(Instant.now());
        conversationRepository.save(conversation);
        taskSessionService.setHumanTakeover(conversationReference, true);
        publishTakeoverRequested(conversation);
        return getDetail(businessId, conversationReference, httpServletRequest);
    }

    @Transactional
    public BusinessConversationDetailDto releaseTakeover(String businessId, String conversationReference, HttpServletRequest httpServletRequest) {
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
        Conversation conversation = findVisibleConversation(businessId, conversationReference);
        conversation.setHumanTakeover(Boolean.FALSE);
        conversation.setStatus(resolveReleaseStatus(conversation));
        conversation.setLastSessionEvent("HUMAN_TAKEOVER_RELEASED");
        conversation.setLastSessionEventAt(Instant.now());
        if (conversation.getActiveTaskKey() == null || conversation.getActiveTaskKey().isBlank()) {
            conversation.setActiveTaskKey("MENU");
        }
        conversationRepository.save(conversation);
        taskSessionService.setHumanTakeover(conversationReference, false);
        publishTakeoverReleased(conversation);
        return getDetail(businessId, conversationReference, httpServletRequest);
    }

    @Transactional
    public BusinessConversationDetailDto updateActiveBusiness(
            String businessId,
            String conversationReference,
            UpdateConversationActiveBusinessRequest request,
            HttpServletRequest httpServletRequest) {
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, request.getActiveBusinessId());
        Conversation conversation = findVisibleConversation(businessId, conversationReference);
        if (conversation.getConversationMode() == ConversationMode.DEDICATED_BUSINESS) {
            throw new BusinessException("Active business can only be reassigned for shared conversations.");
        }
        conversation.setBusinessId(request.getActiveBusinessId());
        conversation.setActiveBusinessId(request.getActiveBusinessId());
        conversation.setLastSessionEvent("ACTIVE_BUSINESS_REASSIGNED");
        conversation.setLastSessionEventAt(Instant.now());
        conversationRepository.save(conversation);
        return getDetail(businessId, conversationReference, httpServletRequest);
    }

    private Conversation findVisibleConversation(String businessId, String conversationReference) {
        return conversationRepository.findVisibleByBusinessIdAndReference(businessId, conversationReference)
                .orElseThrow(() -> new BusinessException("Conversation not found for this business."));
    }

    private ConversationStatus resolveReleaseStatus(Conversation conversation) {
        if (conversation.getSessionExpiresAt() != null && conversation.getSessionExpiresAt().isBefore(Instant.now())) {
            return ConversationStatus.ACTIVE;
        }
        return ConversationStatus.AWAITING_USER;
    }

    private BusinessConversationSummaryDto toSummary(Conversation conversation, String businessId) {
        List<Message> recentMessages = messageRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc(conversation.getReference());
        Message lastInbound = recentMessages.stream()
                .filter(message -> message.getMessageType() == MessageType.INBOUND)
                .max(Comparator.comparing(Message::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        Message lastOutbound = recentMessages.stream()
                .filter(message -> message.getMessageType() == MessageType.OUTBOUND)
                .max(Comparator.comparing(Message::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        return BusinessConversationSummaryDto.builder()
                .reference(conversation.getReference())
                .customerId(conversation.getCustomerId())
                .whatsappConversationId(conversation.getWhatsappConversationId())
                .channelKey(conversation.getChannelKey())
                .conversationMode(conversation.getConversationMode())
                .status(conversation.getStatus())
                .entryBusinessId(conversation.getEntryBusinessId())
                .activeBusinessId(conversation.getActiveBusinessId())
                .entrySlug(conversation.getEntrySlug())
                .marketplaceMode(conversation.getMarketplaceMode())
                .humanTakeover(conversation.getHumanTakeover())
                .lastIntent(conversation.getLastIntent())
                .activeTaskKey(conversation.getActiveTaskKey())
                .lastInteractionAt(conversation.getLastInteractionAt())
                .sessionExpiresAt(conversation.getSessionExpiresAt())
                .reopenCount(conversation.getReopenCount())
                .lastReopenedAt(conversation.getLastReopenedAt())
                .lastSessionEvent(conversation.getLastSessionEvent())
                .lastSessionEventAt(conversation.getLastSessionEventAt())
                .lastInboundMessage(lastInbound != null ? lastInbound.getContent() : null)
                .lastOutboundMessage(lastOutbound != null ? lastOutbound.getContent() : null)
                .lastInboundAt(lastInbound != null ? lastInbound.getCreatedAt() : null)
                .lastOutboundAt(lastOutbound != null ? lastOutbound.getCreatedAt() : null)
                .attribution(resolveAttribution(conversation, businessId))
                .build();
    }

    private ConversationBusinessAttribution resolveAttribution(Conversation conversation, String businessId) {
        boolean entry = businessId.equals(conversation.getEntryBusinessId());
        boolean active = businessId.equals(conversation.getActiveBusinessId()) || businessId.equals(conversation.getBusinessId());
        if (entry && active) {
            return ConversationBusinessAttribution.ENTRY_AND_ACTIVE;
        }
        if (entry) {
            return ConversationBusinessAttribution.ENTRY;
        }
        if (active) {
            return ConversationBusinessAttribution.ACTIVE;
        }
        return ConversationBusinessAttribution.NONE;
    }

    private MessageDto toMessageDto(Message message) {
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

    private void publishTakeoverRequested(Conversation conversation) {
        BusinessDto business = resolveBusiness(conversation.getActiveBusinessId() != null ? conversation.getActiveBusinessId() : conversation.getBusinessId());
        if (business == null) {
            return;
        }
        CustomerDto customer = safeCustomer(conversation.getCustomerId());
        notificationEventPublisher.publishHumanTakeoverRequested(
                recipientForBusiness(business),
                business,
                customer,
                conversation.getReference(),
                "Human takeover requested for conversation " + conversation.getReference() + "."
        );
    }

    private void publishTakeoverReleased(Conversation conversation) {
        BusinessDto business = resolveBusiness(conversation.getActiveBusinessId() != null ? conversation.getActiveBusinessId() : conversation.getBusinessId());
        if (business == null) {
            return;
        }
        CustomerDto customer = safeCustomer(conversation.getCustomerId());
        notificationEventPublisher.publishHumanTakeoverReleased(
                recipientForBusiness(business),
                business,
                customer,
                conversation.getReference(),
                "Human takeover released for conversation " + conversation.getReference() + "."
        );
    }

    private BusinessDto resolveBusiness(String businessId) {
        if (businessId == null || businessId.isBlank()) {
            return null;
        }
        try {
            return businessService.findByBusinessId(businessId);
        } catch (BusinessException ex) {
            return null;
        }
    }

    private CustomerDto safeCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return null;
        }
        try {
            return customerService.findById(customerId);
        } catch (BusinessException ex) {
            return null;
        }
    }

    private String recipientForBusiness(BusinessDto business) {
        if (business == null) {
            return null;
        }
        return business.getWhatsappNumber() != null && !business.getWhatsappNumber().isBlank()
                ? business.getWhatsappNumber()
                : business.getPhoneNumber();
    }
}
