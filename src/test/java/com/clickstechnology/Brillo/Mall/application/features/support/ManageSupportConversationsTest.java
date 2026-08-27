package com.clickstechnology.Brillo.Mall.application.features.support;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.AssignConversationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.ClaimConversationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.SupportReassignActiveBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.SupportAssignmentStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.features.notifications.NotificationEventPublisher;
import com.clickstechnology.Brillo.Mall.application.features.runtime.TaskSessionService;
import com.clickstechnology.Brillo.Mall.domain.conversation.Conversation;
import com.clickstechnology.Brillo.Mall.domain.conversation.ConversationRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.MessageRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.SupportConversationEvent;
import com.clickstechnology.Brillo.Mall.domain.conversation.SupportConversationEventRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.SupportConversationNoteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageSupportConversationsTest {

    @Mock
    private SupportAccessService supportAccessService;
    @Mock
    private UserService userService;
    @Mock
    private BusinessService businessService;
    @Mock
    private CustomerService customerService;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private SupportConversationNoteRepository supportConversationNoteRepository;
    @Mock
    private SupportConversationEventRepository supportConversationEventRepository;
    @Mock
    private TaskSessionService taskSessionService;
    @Mock
    private NotificationEventPublisher notificationEventPublisher;
    @Mock
    private HttpServletRequest request;

    private ManageSupportConversations manageSupportConversations;
    private UserDto agentUser;
    private UserDto superAdminUser;

    @BeforeEach
    void setUp() {
        manageSupportConversations = new ManageSupportConversations(
                supportAccessService,
                userService,
                businessService,
                customerService,
                conversationRepository,
                messageRepository,
                supportConversationNoteRepository,
                supportConversationEventRepository,
                taskSessionService,
                notificationEventPublisher
        );
        agentUser = UserDto.builder().id("200").roles(List.of("AGENT")).build();
        superAdminUser = UserDto.builder().id("300").roles(List.of("SUPER_ADMIN")).build();
    }

    @Test
    void list_filtersAssignedToMeAndReopenedOnly() {
        Conversation mine = conversation("conv-1", ConversationMode.SHARED_BUSINESS);
        mine.setAssignedSupportUserId("200");
        mine.setAssignmentStatus(SupportAssignmentStatus.ASSIGNED.name());
        mine.setLastReopenedAt(Instant.now());
        mine.setLastInteractionAt(Instant.now());

        Conversation other = conversation("conv-2", ConversationMode.DEDICATED_BUSINESS);
        other.setAssignedSupportUserId("999");
        other.setLastInteractionAt(Instant.now().minusSeconds(60));

        when(supportAccessService.requireSupportUser(request)).thenReturn(agentUser);
        when(conversationRepository.findAll()).thenReturn(List.of(mine, other));
        when(messageRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc(any())).thenReturn(List.of());

        var result = manageSupportConversations.list(null, null, null, true, null, true, null, null, null, 1, 20, request);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getReference()).isEqualTo("conv-1");
    }

    @Test
    void claim_assignsConversationToAgentAndRecordsEvent() {
        Conversation conversation = conversation("conv-claim", ConversationMode.SHARED_BUSINESS);
        when(supportAccessService.requireSupportUser(request)).thenReturn(agentUser);
        when(conversationRepository.findByReference("conv-claim")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc("conv-claim")).thenReturn(List.of());
        when(supportConversationNoteRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc("conv-claim")).thenReturn(List.of());
        when(supportConversationEventRepository.findTop50ByConversation_ReferenceOrderByCreatedAtDesc("conv-claim")).thenReturn(List.of());
        when(taskSessionService.findByConversationReference("conv-claim")).thenReturn(Optional.empty());

        var detail = manageSupportConversations.claim("conv-claim", new ClaimConversationRequest("taking over queue"), request);

        assertThat(detail.getAssignment().getAssignedSupportUserId()).isEqualTo("200");
        assertThat(detail.getAssignment().getAssignmentStatus()).isEqualTo(SupportAssignmentStatus.ASSIGNED);
        ArgumentCaptor<SupportConversationEvent> eventCaptor = ArgumentCaptor.forClass(SupportConversationEvent.class);
        verify(supportConversationEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo("CLAIMED");
    }

    @Test
    void assign_allowsSuperAdminToReassignClaimedConversation() {
        Conversation conversation = conversation("conv-assign", ConversationMode.SHARED_BUSINESS);
        conversation.setAssignedSupportUserId("200");
        when(supportAccessService.requireSupportUser(request)).thenReturn(superAdminUser);
        when(supportAccessService.isSuperAdmin(superAdminUser)).thenReturn(true);
        when(conversationRepository.findByReference("conv-assign")).thenReturn(Optional.of(conversation));
        when(userService.findById("500")).thenReturn(UserDto.builder().id("500").roles(List.of("AGENT")).build());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc("conv-assign")).thenReturn(List.of());
        when(supportConversationNoteRepository.findTop20ByConversation_ReferenceOrderByCreatedAtDesc("conv-assign")).thenReturn(List.of());
        when(supportConversationEventRepository.findTop50ByConversation_ReferenceOrderByCreatedAtDesc("conv-assign")).thenReturn(List.of());
        when(taskSessionService.findByConversationReference("conv-assign")).thenReturn(Optional.empty());

        var detail = manageSupportConversations.assign("conv-assign", new AssignConversationRequest("500", "shift transfer"), request);

        assertThat(detail.getAssignment().getAssignedSupportUserId()).isEqualTo("500");
        ArgumentCaptor<SupportConversationEvent> eventCaptor = ArgumentCaptor.forClass(SupportConversationEvent.class);
        verify(supportConversationEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo("TRANSFERRED");
    }

    @Test
    void reassignActiveBusiness_rejectsDedicatedConversation() {
        Conversation conversation = conversation("conv-dedicated", ConversationMode.DEDICATED_BUSINESS);
        conversation.setAssignedSupportUserId("200");
        when(supportAccessService.requireSupportUser(request)).thenReturn(agentUser);
        when(supportAccessService.isSuperAdmin(agentUser)).thenReturn(false);
        when(conversationRepository.findByReference("conv-dedicated")).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> manageSupportConversations.reassignActiveBusiness(
                "conv-dedicated",
                new SupportReassignActiveBusinessRequest("biz-2", "wrong channel"),
                request
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("shared conversations");

        verify(conversationRepository, never()).save(any());
    }

    private Conversation conversation(String reference, ConversationMode mode) {
        Conversation conversation = Conversation.builder()
                .customerId("cust-1")
                .whatsappConversationId("wa-1")
                .channelKey("2348039999999")
                .conversationMode(mode)
                .humanTakeover(false)
                .marketplaceMode(mode == ConversationMode.SHARED_MARKETPLACE)
                .build();
        conversation.setReference(reference);
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());
        return conversation;
    }
}
