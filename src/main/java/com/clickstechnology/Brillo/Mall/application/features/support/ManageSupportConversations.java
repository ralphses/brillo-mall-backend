package com.clickstechnology.Brillo.Mall.application.features.support;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.MessageDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationAssignmentDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationDetailDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationEventDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationNoteDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationSummaryDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.AddSupportNoteRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.AssignConversationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.ClaimConversationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.ReleaseConversationAssignmentRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.ReleaseHumanTakeoverRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.RequestHumanTakeoverRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.SupportReassignActiveBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.MessageType;
import com.clickstechnology.Brillo.Mall.application.enums.SupportAssignmentStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.features.notifications.NotificationEventPublisher;
import com.clickstechnology.Brillo.Mall.application.features.runtime.TaskSessionService;
import com.clickstechnology.Brillo.Mall.domain.conversation.Conversation;
import com.clickstechnology.Brillo.Mall.domain.conversation.ConversationRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.Message;
import com.clickstechnology.Brillo.Mall.domain.conversation.MessageRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.SupportConversationEvent;
import com.clickstechnology.Brillo.Mall.domain.conversation.SupportConversationEventRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.SupportConversationNote;
import com.clickstechnology.Brillo.Mall.domain.conversation.SupportConversationNoteRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.task.ConversationTaskSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ManageSupportConversations {

    private final SupportAccessService supportAccessService;
    private final UserService userService;
    private final BusinessService businessService;
    private final CustomerService customerService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SupportConversationNoteRepository supportConversationNoteRepository;
    private final SupportConversationEventRepository supportConversationEventRepository;
    private final TaskSessionService taskSessionService;
    private final NotificationEventPublisher notificationEventPublisher;

    @Transactional(readOnly = true)
    public PaginatedResponse<SupportConversationSummaryDto> list(
            ConversationStatus status,
            ConversationMode conversationMode,
            Boolean humanTakeover,
            Boolean assignedToMe,
            Boolean unassignedOnly,
            Boolean reopenedOnly,
            String activeBusinessId,
            String entryBusinessId,
            Instant updatedSince,
            Integer page,
            Integer pageSize,
            HttpServletRequest request) {
        UserDto actor = supportAccessService.requireSupportUser(request);
        List<Conversation> conversations = conversationRepository.findAll().stream()
                .filter(conversation -> status == null || conversation.getStatus() == status)
                .filter(conversation -> conversationMode == null || conversation.getConversationMode() == conversationMode)
                .filter(conversation -> humanTakeover == null || Objects.equals(conversation.getHumanTakeover(), humanTakeover))
                .filter(conversation -> assignedToMe == null || !assignedToMe || actor.getId().equals(conversation.getAssignedSupportUserId()))
                .filter(conversation -> unassignedOnly == null || !unassignedOnly || conversation.getAssignedSupportUserId() == null || conversation.getAssignedSupportUserId().isBlank())
                .filter(conversation -> reopenedOnly == null || !reopenedOnly || conversation.getLastReopenedAt() != null)
                .filter(conversation -> activeBusinessId == null || activeBusinessId.equals(conversation.getActiveBusinessId()))
                .filter(conversation -> entryBusinessId == null || entryBusinessId.equals(conversation.getEntryBusinessId()))
                .filter(conversation -> updatedSince == null || (conversation.getUpdatedAt() != null && !conversation.getUpdatedAt().isBefore(updatedSince)))
                .sorted(Comparator.comparing(Conversation::getLastInteractionAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Conversation::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        int fromIndex = Math.min((safePage - 1) * safePageSize, conversations.size());
        int toIndex = Math.min(fromIndex + safePageSize, conversations.size());
        List<SupportConversationSummaryDto> items = conversations.subList(fromIndex, toIndex).stream()
                .map(this::toSummary)
                .toList();
        int totalPages = conversations.isEmpty() ? 0 : (int) Math.ceil((double) conversations.size() / safePageSize);

        return PaginatedResponse.<SupportConversationSummaryDto>builder()
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
    public SupportConversationDetailDto getDetail(String reference, HttpServletRequest request) {
        supportAccessService.requireSupportUser(request);
        Conversation conversation = findConversation(reference);
        ConversationTaskSession taskSession = taskSessionService.findByConversationReference(reference).orElse(null);
        return toDetail(conversation, taskSession);
    }

    @Transactional
    public SupportConversationDetailDto claim(String reference, ClaimConversationRequest claimRequest, HttpServletRequest request) {
        UserDto actor = supportAccessService.requireSupportUser(request);
        Conversation conversation = findConversation(reference);
        String currentAssignee = conversation.getAssignedSupportUserId();
        if (currentAssignee != null && !currentAssignee.isBlank() && !currentAssignee.equals(actor.getId()) && !supportAccessService.isSuperAdmin(actor)) {
            throw new BusinessException("Only the current assignee or a super admin can claim this conversation.");
        }
        applyAssignment(conversation, actor.getId());
        recordEvent(conversation, "CLAIMED", actor.getId(), claimRequest != null ? claimRequest.getReason() : null, currentAssignee, actor.getId(), null, null, null);
        conversationRepository.save(conversation);
        return toDetail(conversation, taskSessionService.findByConversationReference(reference).orElse(null));
    }

    @Transactional
    public SupportConversationDetailDto assign(String reference, AssignConversationRequest assignRequest, HttpServletRequest request) {
        UserDto actor = supportAccessService.requireSupportUser(request);
        Conversation conversation = findConversation(reference);
        UserDto assignee = requireSupportAssignee(assignRequest.getSupportUserId());
        String currentAssignee = conversation.getAssignedSupportUserId();
        if (!supportAccessService.isSuperAdmin(actor) && currentAssignee != null && !currentAssignee.equals(actor.getId())) {
            throw new BusinessException("Only the current assignee or a super admin can transfer this conversation.");
        }
        applyAssignment(conversation, assignee.getId());
        recordEvent(conversation, currentAssignee == null ? "ASSIGNED" : "TRANSFERRED", actor.getId(), assignRequest.getReason(), currentAssignee, assignee.getId(), null, null, null);
        conversationRepository.save(conversation);
        return toDetail(conversation, taskSessionService.findByConversationReference(reference).orElse(null));
    }

    @Transactional
    public SupportConversationDetailDto unassign(String reference, ReleaseConversationAssignmentRequest releaseRequest, HttpServletRequest request) {
        UserDto actor = supportAccessService.requireSupportUser(request);
        Conversation conversation = findConversation(reference);
        String currentAssignee = conversation.getAssignedSupportUserId();
        if (currentAssignee != null && !currentAssignee.isBlank() && !supportAccessService.isSuperAdmin(actor) && !currentAssignee.equals(actor.getId())) {
            throw new BusinessException("Only the current assignee or a super admin can unassign this conversation.");
        }
        clearAssignment(conversation);
        recordEvent(conversation, "UNASSIGNED", actor.getId(), releaseRequest != null ? releaseRequest.getReason() : null, currentAssignee, null, null, null, null);
        conversationRepository.save(conversation);
        return toDetail(conversation, taskSessionService.findByConversationReference(reference).orElse(null));
    }

    @Transactional
    public SupportConversationDetailDto requestTakeover(String reference, RequestHumanTakeoverRequest takeoverRequest, HttpServletRequest request) {
        UserDto actor = supportAccessService.requireSupportUser(request);
        Conversation conversation = findConversation(reference);
        ensureAgentCanOperate(actor, conversation);
        conversation.setHumanTakeover(Boolean.TRUE);
        conversation.setStatus(ConversationStatus.HUMAN_TAKEOVER);
        conversation.setLastSessionEvent("HUMAN_TAKEOVER_REQUESTED");
        conversation.setLastSessionEventAt(Instant.now());
        conversation.setLastSupportActionAt(Instant.now());
        taskSessionService.setHumanTakeover(reference, true);
        recordEvent(conversation, "HUMAN_TAKEOVER_REQUESTED", actor.getId(), takeoverRequest != null ? takeoverRequest.getReason() : null, conversation.getAssignedSupportUserId(), conversation.getAssignedSupportUserId(), null, null, null);
        conversationRepository.save(conversation);
        publishTakeoverRequested(conversation);
        return toDetail(conversation, taskSessionService.findByConversationReference(reference).orElse(null));
    }

    @Transactional
    public SupportConversationDetailDto releaseTakeover(String reference, ReleaseHumanTakeoverRequest releaseRequest, HttpServletRequest request) {
        UserDto actor = supportAccessService.requireSupportUser(request);
        Conversation conversation = findConversation(reference);
        ensureAgentCanOperate(actor, conversation);
        conversation.setHumanTakeover(Boolean.FALSE);
        conversation.setStatus(resolveReleaseStatus(conversation));
        conversation.setLastSessionEvent("HUMAN_TAKEOVER_RELEASED");
        conversation.setLastSessionEventAt(Instant.now());
        conversation.setLastSupportActionAt(Instant.now());
        if (conversation.getActiveTaskKey() == null || conversation.getActiveTaskKey().isBlank()) {
            conversation.setActiveTaskKey("MENU");
        }
        taskSessionService.setHumanTakeover(reference, false);
        recordEvent(conversation, "HUMAN_TAKEOVER_RELEASED", actor.getId(), releaseRequest != null ? releaseRequest.getReason() : null, conversation.getAssignedSupportUserId(), conversation.getAssignedSupportUserId(), null, null, null);
        conversationRepository.save(conversation);
        publishTakeoverReleased(conversation);
        return toDetail(conversation, taskSessionService.findByConversationReference(reference).orElse(null));
    }

    @Transactional
    public SupportConversationNoteDto addNote(String reference, AddSupportNoteRequest addSupportNoteRequest, HttpServletRequest request) {
        UserDto actor = supportAccessService.requireSupportUser(request);
        Conversation conversation = findConversation(reference);
        SupportConversationNote note = supportConversationNoteRepository.save(SupportConversationNote.builder()
                .conversation(conversation)
                .actorUserId(actor.getId())
                .content(addSupportNoteRequest.getContent())
                .build());
        conversation.setLastSupportActionAt(Instant.now());
        conversationRepository.save(conversation);
        recordEvent(conversation, "NOTE_ADDED", actor.getId(), null, conversation.getAssignedSupportUserId(), conversation.getAssignedSupportUserId(), null, null, addSupportNoteRequest.getContent());
        return toNoteDto(note);
    }

    @Transactional(readOnly = true)
    public List<SupportConversationEventDto> history(String reference, HttpServletRequest request) {
        supportAccessService.requireSupportUser(request);
        findConversation(reference);
        return supportConversationEventRepository.findTop50ByConversation_ReferenceOrderByCreatedAtDesc(reference).stream()
                .map(this::toEventDto)
                .toList();
    }

    @Transactional
    public SupportConversationDetailDto reassignActiveBusiness(String reference, SupportReassignActiveBusinessRequest supportRequest, HttpServletRequest request) {
        UserDto actor = supportAccessService.requireSupportUser(request);
        Conversation conversation = findConversation(reference);
        ensureAgentCanOperate(actor, conversation);
        if (conversation.getConversationMode() == ConversationMode.DEDICATED_BUSINESS) {
            throw new BusinessException("Active business can only be reassigned for shared conversations.");
        }
        String fromActiveBusinessId = conversation.getActiveBusinessId();
        businessService.findByBusinessId(supportRequest.getActiveBusinessId());
        conversation.setBusinessId(supportRequest.getActiveBusinessId());
        conversation.setActiveBusinessId(supportRequest.getActiveBusinessId());
        conversation.setLastSessionEvent("ACTIVE_BUSINESS_REASSIGNED");
        conversation.setLastSessionEventAt(Instant.now());
        conversation.setLastSupportActionAt(Instant.now());
        recordEvent(conversation, "ACTIVE_BUSINESS_REASSIGNED", actor.getId(), supportRequest.getReason(), conversation.getAssignedSupportUserId(), conversation.getAssignedSupportUserId(), fromActiveBusinessId, supportRequest.getActiveBusinessId(), null);
        conversationRepository.save(conversation);
        return toDetail(conversation, taskSessionService.findByConversationReference(reference).orElse(null));
    }

    private void ensureAgentCanOperate(UserDto actor, Conversation conversation) {
        if (supportAccessService.isSuperAdmin(actor)) {
            return;
        }
        if (conversation.getAssignedSupportUserId() != null
                && !conversation.getAssignedSupportUserId().isBlank()
                && !conversation.getAssignedSupportUserId().equals(actor.getId())) {
            throw new BusinessException("This conversation is assigned to another support user.");
        }
    }

    private UserDto requireSupportAssignee(String supportUserId) {
        UserDto user = userService.findById(supportUserId);
        if (user.getRoles() == null || (!user.getRoles().contains("AGENT") && !user.getRoles().contains("SUPER_ADMIN"))) {
            throw new BusinessException("Assigned user must have support access.");
        }
        return user;
    }

    private Conversation findConversation(String reference) {
        return conversationRepository.findByReference(reference)
                .orElseThrow(() -> new BusinessException("Conversation not found."));
    }

    private void applyAssignment(Conversation conversation, String supportUserId) {
        conversation.setAssignedSupportUserId(supportUserId);
        conversation.setAssignedSupportAt(Instant.now());
        conversation.setAssignmentStatus(SupportAssignmentStatus.ASSIGNED.name());
        conversation.setLastSupportActionAt(Instant.now());
    }

    private void clearAssignment(Conversation conversation) {
        conversation.setAssignedSupportUserId(null);
        conversation.setAssignedSupportAt(null);
        conversation.setAssignmentStatus(SupportAssignmentStatus.UNASSIGNED.name());
        conversation.setLastSupportActionAt(Instant.now());
    }

    private ConversationStatus resolveReleaseStatus(Conversation conversation) {
        if (conversation.getSessionExpiresAt() != null && conversation.getSessionExpiresAt().isBefore(Instant.now())) {
            return ConversationStatus.ACTIVE;
        }
        return ConversationStatus.AWAITING_USER;
    }

    private SupportConversationSummaryDto toSummary(Conversation conversation) {
        List<Message> recentMessages = messageRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc(conversation.getReference());
        Message lastInbound = recentMessages.stream()
                .filter(message -> message.getMessageType() == MessageType.INBOUND)
                .max(Comparator.comparing(Message::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        Message lastOutbound = recentMessages.stream()
                .filter(message -> message.getMessageType() == MessageType.OUTBOUND)
                .max(Comparator.comparing(Message::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        return SupportConversationSummaryDto.builder()
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
                .reopenCount(conversation.getReopenCount())
                .lastReopenedAt(conversation.getLastReopenedAt())
                .lastSessionEvent(conversation.getLastSessionEvent())
                .lastSessionEventAt(conversation.getLastSessionEventAt())
                .lastInboundMessage(lastInbound != null ? lastInbound.getContent() : null)
                .lastOutboundMessage(lastOutbound != null ? lastOutbound.getContent() : null)
                .lastInboundAt(lastInbound != null ? lastInbound.getCreatedAt() : null)
                .lastOutboundAt(lastOutbound != null ? lastOutbound.getCreatedAt() : null)
                .lastInteractionAt(conversation.getLastInteractionAt())
                .assignment(SupportConversationAssignmentDto.builder()
                        .assignmentStatus(resolveAssignmentStatus(conversation))
                        .assignedSupportUserId(conversation.getAssignedSupportUserId())
                        .assignedSupportAt(conversation.getAssignedSupportAt())
                        .lastSupportActionAt(conversation.getLastSupportActionAt())
                        .build())
                .build();
    }

    private SupportConversationDetailDto toDetail(Conversation conversation, ConversationTaskSession taskSession) {
        SupportConversationSummaryDto summary = toSummary(conversation);
        List<MessageDto> recentMessages = messageRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc(conversation.getReference()).stream()
                .map(this::toMessageDto)
                .sorted(Comparator.comparing(MessageDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<SupportConversationNoteDto> notes = supportConversationNoteRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc(conversation.getReference()).stream()
                .map(this::toNoteDto)
                .toList();
        List<SupportConversationEventDto> history = supportConversationEventRepository.findTop50ByConversation_ReferenceOrderByCreatedAtDesc(conversation.getReference()).stream()
                .map(this::toEventDto)
                .toList();
        return SupportConversationDetailDto.builder()
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
                .reopenCount(summary.getReopenCount())
                .lastReopenedAt(summary.getLastReopenedAt())
                .lastSessionEvent(summary.getLastSessionEvent())
                .lastSessionEventAt(summary.getLastSessionEventAt())
                .lastInboundMessage(summary.getLastInboundMessage())
                .lastOutboundMessage(summary.getLastOutboundMessage())
                .lastInboundAt(summary.getLastInboundAt())
                .lastOutboundAt(summary.getLastOutboundAt())
                .lastInteractionAt(summary.getLastInteractionAt())
                .assignment(summary.getAssignment())
                .lastIntent(conversation.getLastIntent())
                .activeTaskKey(conversation.getActiveTaskKey())
                .currentStateKey(taskSession != null ? taskSession.getCurrentStateKey() : null)
                .taskStatus(taskSession != null ? taskSession.getTaskStatus() : null)
                .sessionExpiresAt(conversation.getSessionExpiresAt())
                .recentMessages(recentMessages)
                .latestNotes(notes)
                .history(history)
                .build();
    }

    private SupportAssignmentStatus resolveAssignmentStatus(Conversation conversation) {
        if (conversation.getAssignmentStatus() != null && !conversation.getAssignmentStatus().isBlank()) {
            return SupportAssignmentStatus.valueOf(conversation.getAssignmentStatus());
        }
        return conversation.getAssignedSupportUserId() == null || conversation.getAssignedSupportUserId().isBlank()
                ? SupportAssignmentStatus.UNASSIGNED
                : SupportAssignmentStatus.ASSIGNED;
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

    private SupportConversationNoteDto toNoteDto(SupportConversationNote note) {
        return SupportConversationNoteDto.builder()
                .reference(note.getReference())
                .actorUserId(note.getActorUserId())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .build();
    }

    private SupportConversationEventDto toEventDto(SupportConversationEvent event) {
        return SupportConversationEventDto.builder()
                .reference(event.getReference())
                .eventType(event.getEventType())
                .actorUserId(event.getActorUserId())
                .reason(event.getReason())
                .fromAssignedSupportUserId(event.getFromAssignedSupportUserId())
                .toAssignedSupportUserId(event.getToAssignedSupportUserId())
                .fromActiveBusinessId(event.getFromActiveBusinessId())
                .toActiveBusinessId(event.getToActiveBusinessId())
                .metadata(event.getMetadata())
                .createdAt(event.getCreatedAt())
                .build();
    }

    private void recordEvent(
            Conversation conversation,
            String eventType,
            String actorUserId,
            String reason,
            String fromAssignedSupportUserId,
            String toAssignedSupportUserId,
            String fromActiveBusinessId,
            String toActiveBusinessId,
            String metadata) {
        supportConversationEventRepository.save(SupportConversationEvent.builder()
                .conversation(conversation)
                .eventType(eventType)
                .actorUserId(actorUserId)
                .reason(reason)
                .fromAssignedSupportUserId(fromAssignedSupportUserId)
                .toAssignedSupportUserId(toAssignedSupportUserId)
                .fromActiveBusinessId(fromActiveBusinessId)
                .toActiveBusinessId(toActiveBusinessId)
                .metadata(metadata)
                .build());
    }

    private void publishTakeoverRequested(Conversation conversation) {
        BusinessDto business = resolveBusiness(conversation.getActiveBusinessId() != null ? conversation.getActiveBusinessId() : conversation.getBusinessId());
        if (business == null) {
            return;
        }
        CustomerDto customer = resolveCustomer(conversation.getCustomerId());
        notificationEventPublisher.publishHumanTakeoverRequested(
                recipientForBusiness(business),
                business,
                customer,
                conversation.getReference(),
                "Support requested human takeover for conversation " + conversation.getReference() + "."
        );
    }

    private void publishTakeoverReleased(Conversation conversation) {
        BusinessDto business = resolveBusiness(conversation.getActiveBusinessId() != null ? conversation.getActiveBusinessId() : conversation.getBusinessId());
        if (business == null) {
            return;
        }
        CustomerDto customer = resolveCustomer(conversation.getCustomerId());
        notificationEventPublisher.publishHumanTakeoverReleased(
                recipientForBusiness(business),
                business,
                customer,
                conversation.getReference(),
                "Support released human takeover for conversation " + conversation.getReference() + "."
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

    private CustomerDto resolveCustomer(String customerId) {
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
