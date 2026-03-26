package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.dto.response.business.OnboardBusinessResponse;
import com.clickstechnology.Brillo.Mall.application.features.business.OnboardUserBusiness;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/businesses")
public class BusinessController {

    private final OnboardUserBusiness onboardUserBusiness;

    @PostMapping("onboard")
    public ResponseWrapper<OnboardBusinessResponse> onboard(
            @RequestBody @Valid final OnboardBusinessRequest request,
            final HttpServletRequest httpServletRequest) {
        OnboardBusinessResponse response = onboardUserBusiness.execute(request, httpServletRequest);
        return success(response);
    }

    @PostMapping(
            value = "{businessId}/upload-logo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseWrapper<OnboardBusinessResponse> uploadLogo(
            @RequestParam("logoFile") final MultipartFile logoFile,
            @PathVariable final String businessId,
            final HttpServletRequest httpServletRequest) {
        OnboardBusinessResponse response = onboardUserBusiness.uploadLogo(logoFile, businessId, httpServletRequest);
        return success(response);
    }

    @PutMapping("{businessId}")
    public ResponseWrapper<OnboardBusinessResponse> updateBusiness(
            @PathVariable final String businessId,
            @RequestBody @Valid final UpdateBusinessRequest request,
            final HttpServletRequest httpServletRequest) {
        OnboardBusinessResponse response = onboardUserBusiness.updateBusiness(businessId, request, httpServletRequest);
        return success(response);
    }

    @GetMapping()
    public ResponseWrapper<PaginatedResponse<BusinessDto>> getBusinesses(
            @RequestParam(value = "page", defaultValue = "1") final Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20")  final Integer pageSize,
            final HttpServletRequest httpServletRequest) {
        PaginatedResponse<BusinessDto> response = onboardUserBusiness.getBusinesses(
                page, pageSize, httpServletRequest);
        return success(response);
    }

    @GetMapping("{businessId}")
    public ResponseWrapper<BusinessDto> getBusinessById(@PathVariable final String businessId) {
        BusinessDto response = onboardUserBusiness.getBusinessById(businessId);
        return success(response);
    }

    @GetMapping("slug/{businessSlug}")
    public ResponseWrapper<BusinessDto> getBusinessBySlug(@PathVariable final String businessSlug) {
        BusinessDto response = onboardUserBusiness.getBusinessBySlug(businessSlug);
        return success(response);
    }
}