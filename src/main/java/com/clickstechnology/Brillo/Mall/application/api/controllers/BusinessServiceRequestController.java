package com.clickstechnology.Brillo.Mall.application.api.controllers;


import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.features.business.ManageBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.features.business.PlaceBusinessServiceRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("api/v1/business-service-requests")
public class BusinessServiceRequestController {

    private final PlaceBusinessServiceRequest placeBusinessServiceRequest;
    private final ManageBusinessServiceRequest manageBusinessServiceRequest;

    @PostMapping
    public ResponseWrapper<String> placeBusinessServiceRequest(
            @RequestBody @Valid PlaceBusinessServiceRequestPayload request,
            final HttpServletRequest httpServletRequest) {
        placeBusinessServiceRequest.execute(request, httpServletRequest);
        return success("Business service request placed successfully");
    }

    @PutMapping("{requestId}")
    public ResponseWrapper<BusinessServiceRequestDto> update(
            @PathVariable String requestId,
            @RequestBody @Valid PlaceBusinessServiceRequestPayload request,
            final HttpServletRequest httpServletRequest) {
        BusinessServiceRequestDto response= manageBusinessServiceRequest.update(
                request,
                httpServletRequest,
                requestId,
                true);
        return success(response);
    }

    @GetMapping
    public ResponseWrapper<PaginatedResponse<BusinessServiceRequestDto>> listBusinessServiceRequests(
            final HttpServletRequest httpServletRequest,
            @RequestParam(required = false) final String businessId,
            @RequestParam(required = false) final String businessServiceId,
            @RequestParam(required = false, defaultValue = "false") final boolean isBusiness,
            @RequestParam(defaultValue = "1") final Integer page,
            @RequestParam(defaultValue = "10") final Integer pageSize) {
        return success(manageBusinessServiceRequest.list(businessId, businessServiceId, isBusiness, page, pageSize, httpServletRequest));
    }

    @DeleteMapping("{requestId}")
    public ResponseWrapper<String> delete(
            @PathVariable String requestId,
            final HttpServletRequest httpServletRequest) {
       manageBusinessServiceRequest.delete(
                httpServletRequest,
                requestId);
        return success("Request deleted successfully");
    }

    @GetMapping("{requestId}")
    public ResponseWrapper<BusinessServiceRequestDto> getRequest(
            @PathVariable String requestId,
            final HttpServletRequest httpServletRequest) {
        return success(manageBusinessServiceRequest.getRequest(
                requestId,
                httpServletRequest));
    }
}
