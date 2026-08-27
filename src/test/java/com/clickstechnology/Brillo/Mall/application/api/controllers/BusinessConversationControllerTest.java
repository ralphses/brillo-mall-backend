package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.conversation.BusinessConversationDetailDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.BusinessConversationSummaryDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.UpdateConversationActiveBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationBusinessAttribution;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.features.business.ManageBusinessConversations;
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
class BusinessConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageBusinessConversations manageBusinessConversations;

    private BusinessConversationSummaryDto summaryDto;
    private BusinessConversationDetailDto detailDto;

    @BeforeEach
    void setUp() {
        Instant now = Instant.parse("2026-08-26T12:00:00Z");
        summaryDto = BusinessConversationSummaryDto.builder()
                .reference("conv-123")
                .customerId("cust-123")
                .channelKey("2348039999999")
                .conversationMode(ConversationMode.SHARED_BUSINESS)
                .status(ConversationStatus.AWAITING_USER)
                .entryBusinessId("biz-123")
                .activeBusinessId("biz-123")
                .entrySlug("test-business")
                .marketplaceMode(false)
                .humanTakeover(false)
                .activeTaskKey("MENU")
                .reopenCount(1)
                .lastSessionEvent("REOPENED_AFTER_EXPIRY")
                .lastSessionEventAt(now)
                .lastInboundMessage("Hi")
                .lastOutboundMessage("Welcome back")
                .attribution(ConversationBusinessAttribution.ENTRY_AND_ACTIVE)
                .build();
        detailDto = BusinessConversationDetailDto.builder()
                .reference(summaryDto.getReference())
                .customerId(summaryDto.getCustomerId())
                .channelKey(summaryDto.getChannelKey())
                .conversationMode(summaryDto.getConversationMode())
                .status(summaryDto.getStatus())
                .entryBusinessId(summaryDto.getEntryBusinessId())
                .activeBusinessId(summaryDto.getActiveBusinessId())
                .entrySlug(summaryDto.getEntrySlug())
                .marketplaceMode(summaryDto.getMarketplaceMode())
                .humanTakeover(summaryDto.getHumanTakeover())
                .activeTaskKey(summaryDto.getActiveTaskKey())
                .reopenCount(summaryDto.getReopenCount())
                .lastSessionEvent(summaryDto.getLastSessionEvent())
                .lastSessionEventAt(summaryDto.getLastSessionEventAt())
                .attribution(summaryDto.getAttribution())
                .recentMessages(List.of())
                .build();
    }

    @Test
    void list_ShouldReturnConversationSummaries() throws Exception {
        PaginatedResponse<BusinessConversationSummaryDto> response = PaginatedResponse.<BusinessConversationSummaryDto>builder()
                .page(1)
                .perPage(20)
                .total(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .items(List.of(summaryDto))
                .build();
        when(manageBusinessConversations.list(eq("biz-123"), eq(true), eq(true), eq(1), eq(20), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/businesses/{businessId}/conversations", "biz-123")
                        .param("humanTakeover", "true")
                        .param("reopenedOnly", "true")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].reference").value("conv-123"))
                .andExpect(jsonPath("$.data.items[0].conversationMode").value("SHARED_BUSINESS"))
                .andExpect(jsonPath("$.data.items[0].attribution").value("ENTRY_AND_ACTIVE"))
                .andExpect(jsonPath("$.data.items[0].lastSessionEvent").value("REOPENED_AFTER_EXPIRY"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void detail_ShouldReturnConversationDetail() throws Exception {
        when(manageBusinessConversations.getDetail(eq("biz-123"), eq("conv-123"), any())).thenReturn(detailDto);

        mockMvc.perform(get("/api/v1/businesses/{businessId}/conversations/{conversationReference}", "biz-123", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reference").value("conv-123"))
                .andExpect(jsonPath("$.data.entryBusinessId").value("biz-123"))
                .andExpect(jsonPath("$.data.activeBusinessId").value("biz-123"))
                .andExpect(jsonPath("$.data.lastSessionEvent").value("REOPENED_AFTER_EXPIRY"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void requestTakeover_ShouldReturnUpdatedConversation() throws Exception {
        BusinessConversationDetailDto takeoverDto = BusinessConversationDetailDto.builder()
                .reference(detailDto.getReference())
                .status(ConversationStatus.HUMAN_TAKEOVER)
                .humanTakeover(true)
                .lastSessionEvent("HUMAN_TAKEOVER_REQUESTED")
                .conversationMode(ConversationMode.SHARED_BUSINESS)
                .attribution(ConversationBusinessAttribution.ENTRY_AND_ACTIVE)
                .recentMessages(List.of())
                .build();
        when(manageBusinessConversations.requestTakeover(eq("biz-123"), eq("conv-123"), any())).thenReturn(takeoverDto);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/conversations/{conversationReference}/takeover", "biz-123", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("HUMAN_TAKEOVER"))
                .andExpect(jsonPath("$.data.humanTakeover").value(true))
                .andExpect(jsonPath("$.data.lastSessionEvent").value("HUMAN_TAKEOVER_REQUESTED"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void releaseTakeover_ShouldReturnResumedConversation() throws Exception {
        BusinessConversationDetailDto releaseDto = BusinessConversationDetailDto.builder()
                .reference(detailDto.getReference())
                .status(ConversationStatus.AWAITING_USER)
                .humanTakeover(false)
                .activeTaskKey("MENU")
                .lastSessionEvent("HUMAN_TAKEOVER_RELEASED")
                .conversationMode(ConversationMode.SHARED_BUSINESS)
                .attribution(ConversationBusinessAttribution.ENTRY_AND_ACTIVE)
                .recentMessages(List.of())
                .build();
        when(manageBusinessConversations.releaseTakeover(eq("biz-123"), eq("conv-123"), any())).thenReturn(releaseDto);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/conversations/{conversationReference}/release", "biz-123", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("AWAITING_USER"))
                .andExpect(jsonPath("$.data.humanTakeover").value(false))
                .andExpect(jsonPath("$.data.activeTaskKey").value("MENU"))
                .andExpect(jsonPath("$.data.lastSessionEvent").value("HUMAN_TAKEOVER_RELEASED"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updateActiveBusiness_ShouldReturnUpdatedSharedConversation() throws Exception {
        UpdateConversationActiveBusinessRequest request = new UpdateConversationActiveBusinessRequest("biz-456");
        BusinessConversationDetailDto updatedDto = BusinessConversationDetailDto.builder()
                .reference(detailDto.getReference())
                .conversationMode(ConversationMode.SHARED_BUSINESS)
                .entryBusinessId("biz-123")
                .activeBusinessId("biz-456")
                .lastSessionEvent("ACTIVE_BUSINESS_REASSIGNED")
                .attribution(ConversationBusinessAttribution.ENTRY)
                .recentMessages(List.of())
                .build();
        when(manageBusinessConversations.updateActiveBusiness(eq("biz-123"), eq("conv-123"), any(UpdateConversationActiveBusinessRequest.class), any()))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/businesses/{businessId}/conversations/{conversationReference}/active-business", "biz-123", "conv-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeBusinessId").value("biz-456"))
                .andExpect(jsonPath("$.data.lastSessionEvent").value("ACTIVE_BUSINESS_REASSIGNED"))
                .andExpect(jsonPath("$.status").value(200));
    }
}
