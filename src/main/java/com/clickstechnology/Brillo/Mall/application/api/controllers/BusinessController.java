package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.DashboardData;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.dto.response.business.OnboardBusinessResponse;
import com.clickstechnology.Brillo.Mall.application.api.contracts.DashboardService;
import com.clickstechnology.Brillo.Mall.application.features.business.GetBusinessCustomers;
import com.clickstechnology.Brillo.Mall.application.features.business.OnboardUserBusiness;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/businesses")
@Tag(name = "Businesses", description = "Business onboarding, profile, dashboard, and customer APIs")
@SecurityRequirement(name = "bearerAuth")
public class BusinessController {

    private final OnboardUserBusiness onboardUserBusiness;
    private final GetBusinessCustomers  getBusinessCustomers;
    private final DashboardService dashboardService;

    @PostMapping("onboard")
    @Operation(summary = "Onboard a business")
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
    @Operation(summary = "Upload business logo")
    public ResponseWrapper<OnboardBusinessResponse> uploadLogo(
            @RequestParam("logoFile") final MultipartFile logoFile,
            @PathVariable final String businessId,
            final HttpServletRequest httpServletRequest) {
        OnboardBusinessResponse response = onboardUserBusiness.uploadLogo(logoFile, businessId, httpServletRequest);
        return success(response);
    }

    @PutMapping("{businessId}")
    @Operation(summary = "Update business profile")
    public ResponseWrapper<OnboardBusinessResponse> updateBusiness(
            @PathVariable final String businessId,
            @RequestBody @Valid final UpdateBusinessRequest request,
            final HttpServletRequest httpServletRequest) {
        OnboardBusinessResponse response = onboardUserBusiness.updateBusiness(businessId, request, httpServletRequest);
        return success(response);
    }

    @PostMapping("{businessId}/activate-storefront")
    @Operation(summary = "Activate storefront")
    public ResponseWrapper<OnboardBusinessResponse> activateStorefront(
            @PathVariable final String businessId,
            final HttpServletRequest httpServletRequest) {
        OnboardBusinessResponse response = onboardUserBusiness.activateStorefront(businessId, httpServletRequest);
        return success(response);
    }

    @GetMapping()
    @Operation(summary = "List owned businesses")
    public ResponseWrapper<PaginatedResponse<BusinessDto>> getBusinesses(
            @RequestParam(value = "page", defaultValue = "1") final Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20")  final Integer pageSize,
            final HttpServletRequest httpServletRequest) {
        PaginatedResponse<BusinessDto> response = onboardUserBusiness.getBusinesses(
                page, pageSize, httpServletRequest);
        return success(response);
    }

    @GetMapping("dashboard")
    @Operation(summary = "Get business dashboard")
    public ResponseWrapper<DashboardData> getDashboard(final Authentication authentication) {
        DashboardData response = dashboardService.getDashboardData(authentication);
        return success(response);
    }

    @GetMapping("{businessId}")
    @Operation(summary = "Get business by id")
    public ResponseWrapper<BusinessDto> getBusinessById(@PathVariable final String businessId) {
        BusinessDto response = onboardUserBusiness.getBusinessById(businessId);
        return success(response);
    }

    @GetMapping("slug/{businessSlug}")
    @Operation(summary = "Get business by slug")
    public ResponseWrapper<BusinessDto> getBusinessBySlug(@PathVariable final String businessSlug) {
        BusinessDto response = onboardUserBusiness.getBusinessBySlug(businessSlug);
        return success(response);
    }

    @GetMapping("{businessId}/customers")
    @Operation(summary = "List business customers")
    public ResponseWrapper<PaginatedResponse<CustomerDto>> getCustomers(@PathVariable final String businessId,
        @RequestParam(value = "page", defaultValue = "1") final Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20")  final Integer pageSize,
        final HttpServletRequest httpServletRequest) {
        PaginatedResponse<CustomerDto> response = getBusinessCustomers.execute(
                businessId, page, pageSize, httpServletRequest);
            return success(response);
    }
}
