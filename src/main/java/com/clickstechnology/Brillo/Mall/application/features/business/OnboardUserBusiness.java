package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.MediaAssetService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.business.OnboardBusinessResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.FileType;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardUserBusiness {

    private final UserService userService;
    private final BusinessService businessService;
    private final TenantContextResolver tenantContextResolver;
    private final AppPropertiesConfig appPropertiesConfig;
    private final MediaAssetService mediaAssetService;

    @LoggableRequest
    public OnboardBusinessResponse execute(
            final OnboardBusinessRequest request,
            final HttpServletRequest httpServletRequest) {

        final UserDto user = tenantContextResolver.currentUser(httpServletRequest);

        // Validate category
        final BusinessCategory businessCategory = AppUtils.validateBusinessCategory(request.getBusinessCategory());

        // validate business name
        businessService.ensureBusinessNameDoesNotExist(request.getBusinessName());

        // Create new business
        businessService.createNew(request, user, appPropertiesConfig.getDefaultBusinessLogoUrl(), businessCategory);

        // Add admin role to user
        userService.addRoleToUser(user.getUsername(), UserRole.ADMIN);

        return new OnboardBusinessResponse("Your business has been created and is currently active.");
    }

    @LoggableRequest
    public OnboardBusinessResponse uploadLogo(MultipartFile logoFile, String businessId, HttpServletRequest httpServletRequest) {
        if (logoFile == null || logoFile.isEmpty()) {
            throw new BusinessException("Logo file is empty");
        }

        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
        String logoUrl = mediaAssetService.uploadFile(FileType.LOGO, logoFile, businessId);
        businessService.addLogo(businessId, logoUrl);

        return new OnboardBusinessResponse("Logo has been successfully uploaded.");
    }

    @LoggableRequest
    public OnboardBusinessResponse updateBusiness(
            final String businessId,
            final UpdateBusinessRequest updateBusinessRequest,
            final HttpServletRequest httpServletRequest) {

        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
        businessService.updateBusiness(businessId, updateBusinessRequest);

        return new OnboardBusinessResponse("Business has been successfully updated.");
    }

    @LoggableRequest
    public OnboardBusinessResponse activateStorefront(final String businessId, final HttpServletRequest httpServletRequest) {
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
        businessService.activateStorefront(businessId);
        return new OnboardBusinessResponse("Storefront has been activated.");
    }

    @LoggableRequest
    public PaginatedResponse<BusinessDto> getBusinesses(Integer page, Integer pageSize, HttpServletRequest httpServletRequest) {
        UserDto userDto = tenantContextResolver.currentUser(httpServletRequest);
        return businessService.findAllByOwnerId(userDto.getId(), page, pageSize);
    }

    public BusinessDto getBusinessById(String businessId) {
        return businessService.findByBusinessId(businessId);
    }

    public BusinessDto getBusinessBySlug(String businessSlug) {
        return businessService.findByBusinessSlug(businessSlug);
    }
}
