package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.ServiceRequestStatus;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            final String requestId) {

        UserDto user = getAuthenticatedUser(httpServletRequest);
        List<String> userRoles = user.getRoles();
        BusinessServiceRequestDto businessServiceRequest = businessServiceRequestService.findById(requestId);

        authorizeForRequest(user, userRoles, businessServiceRequest);

        boolean businessActor = userRoles.contains(UserRole.ADMIN.name());
        if (!businessActor && !userRoles.contains(UserRole.USER.name())) {
            throw new UnauthorizedUserException();
        }

        return businessServiceRequestService.updateRequest(requestId, request, businessActor);
    }

    public PaginatedResponse<BusinessServiceRequestDto> list(
            final String businessId,
            final String businessServiceId,
            final ServiceRequestStatus requestStatus,
            final boolean isBusiness,
            final Integer page,
            final Integer pageSize,
            final HttpServletRequest httpServletRequest) {

        UserDto user = getAuthenticatedUser(httpServletRequest);
        List<String> userRoles = user.getRoles();
        Pageable pageable = AppUtils.getPageable(page, pageSize);

        if (userRoles.contains(UserRole.ADMIN.name()) && isBusiness) {
            if (businessId != null) {
                businessService.ensureBusinessBelongsToUser(businessId, user.getId());
                return businessServiceRequestService.listForBusiness(businessId, businessServiceId, requestStatus, pageable);
            }
            if (businessServiceId != null) {
                BusinessServiceDto thisBusinessService = businessServiceService.getByIdOrSlug(businessServiceId);
                businessService.ensureBusinessBelongsToUser(thisBusinessService.getBusinessId(), user.getId());
                return businessServiceRequestService.listForBusinessService(businessServiceId, requestStatus, pageable);
            }

            List<BusinessDto> adminBusinesses = businessService.findAllByOwnerId(user.getId());
            Set<String> businessIds = adminBusinesses.stream().map(BusinessDto::getId).collect(Collectors.toSet());
            return businessServiceRequestService.listForBusinesses(businessIds, requestStatus, pageable);
        }

        return businessServiceRequestService.listForUser(user.getId(), requestStatus, pageable);
    }

    @LoggableRequest
    public void delete(HttpServletRequest httpServletRequest, String requestId) {
        UserDto user = getAuthenticatedUser(httpServletRequest);
        List<String> userRoles = user.getRoles();
        BusinessServiceRequestDto request = businessServiceRequestService.findById(requestId);

        authorizeForRequest(user, userRoles, request);
        businessServiceRequestService.delete(request);
    }

    public BusinessServiceRequestDto getRequest(String requestId, HttpServletRequest httpServletRequest) {
        UserDto user = getAuthenticatedUser(httpServletRequest);
        List<String> userRoles = user.getRoles();
        BusinessServiceRequestDto serviceRequest = businessServiceRequestService.findById(requestId);

        authorizeForRequest(user, userRoles, serviceRequest);
        return serviceRequest;
    }

    private UserDto getAuthenticatedUser(HttpServletRequest httpServletRequest) {
        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        if (authenticatedUsername == null) {
            throw new UnauthorizedUserException();
        }
        return userService.findByUsername(authenticatedUsername);
    }

    private void authorizeForRequest(UserDto user, List<String> userRoles, BusinessServiceRequestDto serviceRequest) {
        if (userRoles.contains(UserRole.ADMIN.name())) {
            businessService.ensureBusinessBelongsToUser(serviceRequest.getBusiness().getId(), user.getId());
        } else if (userRoles.contains(UserRole.USER.name())) {
            if (!serviceRequest.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedUserException();
            }
        } else {
            throw new UnauthorizedUserException();
        }
    }
}
