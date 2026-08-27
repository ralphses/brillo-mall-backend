package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationAssignmentDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationDetailDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationEventDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationNoteDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.SupportConversationSummaryDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.AddSupportNoteRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.AssignConversationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.SupportReassignActiveBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.SupportAssignmentStatus;
import com.clickstechnology.Brillo.Mall.application.features.support.ManageSupportConversations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SupportConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageSupportConversations manageSupportConversations;

    private SupportConversationSummaryDto summaryDto;
    private SupportConversationDetailDto detailDto;

    @BeforeEach
    void setUp() {
        SupportConversationAssignmentDto assignment = SupportConversationAssignmentDto.builder()
                .assignmentStatus(SupportAssignmentStatus.ASSIGNED)
                .assignedSupportUserId("200")
                .assignedSupportAt(Instant.parse("2026-08-27T10:00:00Z"))
                .lastSupportActionAt(Instant.parse("2026-08-27T10:05:00Z"))
                .build();
        summaryDto = SupportConversationSummaryDto.builder()
                .reference("conv-123")
                .customerId("cust-123")
                .whatsappConversationId("wa-123")
                .channelKey("2348039999999")
                .conversationMode(ConversationMode.SHARED_BUSINESS)
                .status(ConversationStatus.HUMAN_TAKEOVER)
                .entryBusinessId("biz-1")
                .activeBusinessId("biz-2")
                .marketplaceMode(false)
                .humanTakeover(true)
                .reopenCount(1)
                .lastSessionEvent("HUMAN_TAKEOVER_REQUESTED")
                .assignment(assignment)
                .build();
        detailDto = SupportConversationDetailDto.builder()
                .reference(summaryDto.getReference())
                .customerId(summaryDto.getCustomerId())
                .whatsappConversationId(summaryDto.getWhatsappConversationId())
                .channelKey(summaryDto.getChannelKey())
                .conversationMode(summaryDto.getConversationMode())
                .status(summaryDto.getStatus())
                .entryBusinessId(summaryDto.getEntryBusinessId())
                .activeBusinessId(summaryDto.getActiveBusinessId())
                .marketplaceMode(summaryDto.getMarketplaceMode())
                .humanTakeover(summaryDto.getHumanTakeover())
                .reopenCount(summaryDto.getReopenCount())
                .lastSessionEvent(summaryDto.getLastSessionEvent())
                .assignment(summaryDto.getAssignment())
                .recentMessages(List.of())
                .latestNotes(List.of())
                .history(List.of())
                .build();
    }

    @Test
    void list_ShouldReturnSupportInbox() throws Exception {
        PaginatedResponse<SupportConversationSummaryDto> response = PaginatedResponse.<SupportConversationSummaryDto>builder()
                .page(1)
                .perPage(20)
                .total(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .items(List.of(summaryDto))
                .build();
        when(manageSupportConversations.list(
                eq(ConversationStatus.HUMAN_TAKEOVER),
                eq(ConversationMode.SHARED_BUSINESS),
                eq(true),
                eq(true),
                eq(false),
                eq(true),
                eq("biz-2"),
                eq("biz-1"),
                any(),
                eq(1),
                eq(20),
                any()
        )).thenReturn(response);

        mockMvc.perform(get("/api/v1/support/conversations")
                        .param("status", "HUMAN_TAKEOVER")
                        .param("conversationMode", "SHARED_BUSINESS")
                        .param("humanTakeover", "true")
                        .param("assignedToMe", "true")
                        .param("unassignedOnly", "false")
                        .param("reopenedOnly", "true")
                        .param("activeBusinessId", "biz-2")
                        .param("entryBusinessId", "biz-1")
                        .param("updatedSince", "2026-08-27T10:00:00Z")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].reference").value("conv-123"))
                .andExpect(jsonPath("$.data.items[0].assignment.assignmentStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void detail_ShouldReturnSupportConversation() throws Exception {
        when(manageSupportConversations.getDetail(eq("conv-123"), any())).thenReturn(detailDto);

        mockMvc.perform(get("/api/v1/support/conversations/{reference}", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reference").value("conv-123"))
                .andExpect(jsonPath("$.data.assignment.assignedSupportUserId").value("200"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void assign_ShouldReturnUpdatedConversation() throws Exception {
        when(manageSupportConversations.assign(eq("conv-123"), any(AssignConversationRequest.class), any())).thenReturn(detailDto);

        mockMvc.perform(post("/api/v1/support/conversations/{reference}/assign", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignConversationRequest("300", "handover"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reference").value("conv-123"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void addNote_ShouldReturnCreatedNote() throws Exception {
        SupportConversationNoteDto noteDto = SupportConversationNoteDto.builder()
                .reference("note-1")
                .actorUserId("200")
                .content("Customer requested callback")
                .build();
        when(manageSupportConversations.addNote(eq("conv-123"), any(AddSupportNoteRequest.class), any())).thenReturn(noteDto);

        mockMvc.perform(post("/api/v1/support/conversations/{reference}/notes", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddSupportNoteRequest("Customer requested callback"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Customer requested callback"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void history_ShouldReturnSupportEvents() throws Exception {
        when(manageSupportConversations.history(eq("conv-123"), any())).thenReturn(List.of(
                SupportConversationEventDto.builder().reference("evt-1").eventType("CLAIMED").actorUserId("200").build()
        ));

        mockMvc.perform(get("/api/v1/support/conversations/{reference}/history", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].eventType").value("CLAIMED"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void reassignActiveBusiness_ShouldReturnUpdatedSharedConversation() throws Exception {
        when(manageSupportConversations.reassignActiveBusiness(eq("conv-123"), any(SupportReassignActiveBusinessRequest.class), any())).thenReturn(detailDto);

        mockMvc.perform(put("/api/v1/support/conversations/{reference}/active-business", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SupportReassignActiveBusinessRequest("biz-3", "switching context"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reference").value("conv-123"))
                .andExpect(jsonPath("$.status").value(200));
    }
}
