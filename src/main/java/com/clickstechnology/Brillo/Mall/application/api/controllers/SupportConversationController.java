package com.clickstechnology.Brillo.Mall.application.api.controllers;

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
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.features.support.ManageSupportConversations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/support/conversations")
@Tag(name = "Support Conversations", description = "Marketplace-wide support inbox and support operations APIs")
@SecurityRequirement(name = "bearerAuth")
public class SupportConversationController {

    private final ManageSupportConversations manageSupportConversations;

    @GetMapping
    @Operation(summary = "List support conversations")
    public ResponseWrapper<PaginatedResponse<SupportConversationSummaryDto>> list(
            @RequestParam(value = "status", required = false) ConversationStatus status,
            @RequestParam(value = "conversationMode", required = false) ConversationMode conversationMode,
            @RequestParam(value = "humanTakeover", required = false) Boolean humanTakeover,
            @RequestParam(value = "assignedToMe", required = false) Boolean assignedToMe,
            @RequestParam(value = "unassignedOnly", required = false) Boolean unassignedOnly,
            @RequestParam(value = "reopenedOnly", required = false) Boolean reopenedOnly,
            @RequestParam(value = "activeBusinessId", required = false) String activeBusinessId,
            @RequestParam(value = "entryBusinessId", required = false) String entryBusinessId,
            @RequestParam(value = "updatedSince", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        return success(manageSupportConversations.list(
                status,
                conversationMode,
                humanTakeover,
                assignedToMe,
                unassignedOnly,
                reopenedOnly,
                activeBusinessId,
                entryBusinessId,
                updatedSince,
                page,
                pageSize,
                request
        ));
    }

    @GetMapping("{reference}")
    @Operation(summary = "Get support conversation detail")
    public ResponseWrapper<SupportConversationDetailDto> detail(@PathVariable String reference, HttpServletRequest request) {
        return success(manageSupportConversations.getDetail(reference, request));
    }

    @PostMapping("{reference}/claim")
    @Operation(summary = "Claim support conversation")
    public ResponseWrapper<SupportConversationDetailDto> claim(
            @PathVariable String reference,
            @RequestBody(required = false) ClaimConversationRequest claimConversationRequest,
            HttpServletRequest request) {
        return success(manageSupportConversations.claim(reference, claimConversationRequest, request));
    }

    @PostMapping("{reference}/assign")
    @Operation(summary = "Assign support conversation")
    public ResponseWrapper<SupportConversationDetailDto> assign(
            @PathVariable String reference,
            @RequestBody @Valid AssignConversationRequest assignConversationRequest,
            HttpServletRequest request) {
        return success(manageSupportConversations.assign(reference, assignConversationRequest, request));
    }

    @PostMapping("{reference}/unassign")
    @Operation(summary = "Unassign support conversation")
    public ResponseWrapper<SupportConversationDetailDto> unassign(
            @PathVariable String reference,
            @RequestBody(required = false) ReleaseConversationAssignmentRequest releaseConversationAssignmentRequest,
            HttpServletRequest request) {
        return success(manageSupportConversations.unassign(reference, releaseConversationAssignmentRequest, request));
    }

    @PostMapping("{reference}/takeover")
    @Operation(summary = "Request support human takeover")
    public ResponseWrapper<SupportConversationDetailDto> requestTakeover(
            @PathVariable String reference,
            @RequestBody(required = false) RequestHumanTakeoverRequest requestHumanTakeoverRequest,
            HttpServletRequest request) {
        return success(manageSupportConversations.requestTakeover(reference, requestHumanTakeoverRequest, request));
    }

    @PostMapping("{reference}/release")
    @Operation(summary = "Release support human takeover")
    public ResponseWrapper<SupportConversationDetailDto> releaseTakeover(
            @PathVariable String reference,
            @RequestBody(required = false) ReleaseHumanTakeoverRequest releaseHumanTakeoverRequest,
            HttpServletRequest request) {
        return success(manageSupportConversations.releaseTakeover(reference, releaseHumanTakeoverRequest, request));
    }

    @PostMapping("{reference}/notes")
    @Operation(summary = "Add support note")
    public ResponseWrapper<SupportConversationNoteDto> addNote(
            @PathVariable String reference,
            @RequestBody @Valid AddSupportNoteRequest addSupportNoteRequest,
            HttpServletRequest request) {
        return success(manageSupportConversations.addNote(reference, addSupportNoteRequest, request));
    }

    @GetMapping("{reference}/history")
    @Operation(summary = "List support conversation history")
    public ResponseWrapper<List<SupportConversationEventDto>> history(@PathVariable String reference, HttpServletRequest request) {
        return success(manageSupportConversations.history(reference, request));
    }

    @PutMapping("{reference}/active-business")
    @Operation(summary = "Reassign active business from support")
    public ResponseWrapper<SupportConversationDetailDto> reassignActiveBusiness(
            @PathVariable String reference,
            @RequestBody @Valid SupportReassignActiveBusinessRequest supportReassignActiveBusinessRequest,
            HttpServletRequest request) {
        return success(manageSupportConversations.reassignActiveBusiness(reference, supportReassignActiveBusinessRequest, request));
    }
}
