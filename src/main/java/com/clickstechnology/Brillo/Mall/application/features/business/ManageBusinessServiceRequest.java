package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageBusinessServiceRequest {

    private final BusinessServiceRequestService businessServiceRequestService;
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;
    private final BusinessService businessService;
    private final BusinessServiceService businessServiceService;

    @LoggableRequest
    public BusinessServiceRequestDto update(
            final PlaceBusinessServiceRequestPayload request,
            final HttpServletRequest httpServletRequest,
            final String requestId,
            final boolean updateNegotiationCounter) {

        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        if (authenticatedUsername == null) {
            throw new UnauthorizedUserException();
        }

        UserDto user = userService.findByUsername(authenticatedUsername);
        List<String> userRoles = user.getRoles();

        if (userRoles == null || userRoles.isEmpty()) {
            throw new UnauthorizedUserException();
        }

        BusinessServiceRequestDto businessServiceRequest = businessServiceRequestService.findById(requestId);

        if (userRoles.contains(UserRole.ADMIN.toString()) && request.isBusiness()) {
            BusinessDto business = businessServiceRequest.getBusiness();
            businessService.ensureBusinessBelongsToUser(business.getId(), user.getId());
        }

        if (userRoles.contains(UserRole.USER.toString())
                && !businessServiceRequest.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedUserException();
        }

        List<EntityStatus> acceptedUserStatus = List.of(EntityStatus.PENDING, EntityStatus.INACTIVE);

        if (!acceptedUserStatus.contains(businessServiceRequest.getStatus()) && !request.isBusiness()) {
            throw new BusinessException("Business service request is invalid or already processed.");
        }

        // Update request
        return businessServiceRequestService.updateRequest(requestId, request, updateNegotiationCounter);

    }


    public PaginatedResponse<BusinessServiceRequestDto> list(
            final String businessId,
            final String businessServiceId,
            final boolean isBusiness,
            final Integer page,
            final Integer pageSize,
            final HttpServletRequest httpServletRequest) {

        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto user = userService.findByUsername(authenticatedUsername);

        List<String> userRoles = user.getRoles();
        Pageable pageable = AppUtils.getPageable(page, pageSize);

        if (userRoles.contains(UserRole.ADMIN.name()) && isBusiness) {
            if (businessId != null) {
                businessService.ensureBusinessBelongsToUser(businessId, user.getId());
                return businessServiceRequestService.listForBusiness(businessId, businessServiceId, pageable);
            }
            if (businessServiceId != null) {
                BusinessServiceDto thisBusinessService = businessServiceService.getByIdOrSlug(businessServiceId);
                businessService.ensureBusinessBelongsToUser(thisBusinessService.getBusinessId(), user.getId());
                return businessServiceRequestService.listForBusinessService(businessServiceId, pageable);
            }
        }

        return businessServiceRequestService.listForUser(user.getId(), pageable);
    }

    @LoggableRequest
    public void delete(HttpServletRequest httpServletRequest, String requestId) {
        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto user = userService.findByUsername(authenticatedUsername);
        List<String> userRoles = user.getRoles();

        BusinessServiceRequestDto request = businessServiceRequestService.findById(requestId);

        if (userRoles.contains(UserRole.ADMIN.name())) {
            businessService.ensureBusinessBelongsToUser(request.getBusiness().getId(), user.getId());
            businessServiceRequestService.delete(request);
        }

        if (userRoles.contains(UserRole.USER.name()) && request.getUser().getId().equals(user.getId())) {
            businessServiceRequestService.delete(request);
        }

    }

    public BusinessServiceRequestDto getRequest(String requestId, HttpServletRequest httpServletRequest) {
        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto user = userService.findByUsername(authenticatedUsername);

        BusinessServiceRequestDto serviceRequest = businessServiceRequestService.findById(requestId);

        if (!serviceRequest.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedUserException();
        }

        return serviceRequest;
    }
}
