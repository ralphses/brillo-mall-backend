package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.conversation.BusinessConversationDetailDto;
import com.clickstechnology.Brillo.Mall.application.dto.conversation.BusinessConversationSummaryDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.conversation.UpdateConversationActiveBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.features.business.ManageBusinessConversations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/businesses/{businessId}/conversations")
@Tag(name = "Business Conversations", description = "Business-scoped conversation monitoring and control APIs")
@SecurityRequirement(name = "bearerAuth")
public class BusinessConversationController {

    private final ManageBusinessConversations manageBusinessConversations;

    @GetMapping
    @Operation(summary = "List business conversations")
    public ResponseWrapper<PaginatedResponse<BusinessConversationSummaryDto>> list(
            @PathVariable String businessId,
            @RequestParam(value = "humanTakeover", required = false) Boolean humanTakeover,
            @RequestParam(value = "reopenedOnly", required = false) Boolean reopenedOnly,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            HttpServletRequest httpServletRequest) {
        return success(manageBusinessConversations.list(businessId, humanTakeover, reopenedOnly, page, pageSize, httpServletRequest));
    }

    @GetMapping("{conversationReference}")
    @Operation(summary = "Get business conversation detail")
    public ResponseWrapper<BusinessConversationDetailDto> detail(
            @PathVariable String businessId,
            @PathVariable String conversationReference,
            HttpServletRequest httpServletRequest) {
        return success(manageBusinessConversations.getDetail(businessId, conversationReference, httpServletRequest));
    }

    @PostMapping("{conversationReference}/takeover")
    @Operation(summary = "Request business conversation takeover")
    public ResponseWrapper<BusinessConversationDetailDto> requestTakeover(
            @PathVariable String businessId,
            @PathVariable String conversationReference,
            HttpServletRequest httpServletRequest) {
        return success(manageBusinessConversations.requestTakeover(businessId, conversationReference, httpServletRequest));
    }

    @PostMapping("{conversationReference}/release")
    @Operation(summary = "Release business conversation takeover")
    public ResponseWrapper<BusinessConversationDetailDto> releaseTakeover(
            @PathVariable String businessId,
            @PathVariable String conversationReference,
            HttpServletRequest httpServletRequest) {
        return success(manageBusinessConversations.releaseTakeover(businessId, conversationReference, httpServletRequest));
    }

    @PutMapping("{conversationReference}/active-business")
    @Operation(summary = "Reassign active business for shared conversation")
    public ResponseWrapper<BusinessConversationDetailDto> updateActiveBusiness(
            @PathVariable String businessId,
            @PathVariable String conversationReference,
            @RequestBody @Valid UpdateConversationActiveBusinessRequest request,
            HttpServletRequest httpServletRequest) {
        return success(manageBusinessConversations.updateActiveBusiness(businessId, conversationReference, request, httpServletRequest));
    }
}
