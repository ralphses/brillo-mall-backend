package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageBusinessService {

    private final BusinessServiceService businessServiceService;
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;
    private final BusinessService businessService;

    @LoggableRequest
    public void update(String serviceId, UpdateBusinessServiceRequest request, HttpServletRequest httpServletRequest) {
        String username = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto userDto = userService.findByUsername(username);
        businessService.ensureBusinessBelongsToUser(request.getBusinessId(), userDto.getId());
        businessServiceService.updateService(serviceId, request, userDto);
    }

    public PaginatedResponse<BusinessServiceDto> listBusinessServices(String businessId, Integer page, Integer pageSize) {
        Pageable pageable = AppUtils.getPageable(page, pageSize);
        return businessServiceService.listServices(businessId, pageable);
    }

    @LoggableRequest
    public BusinessServiceDto getOneService(String serviceIdOrSlug, final HttpServletRequest httpServletRequest) {
        return businessServiceService.getByIdOrSlug(serviceIdOrSlug);
    }

    @LoggableRequest
    public void deleteById(String serviceId, HttpServletRequest httpServletRequest) {
        String username = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto userDto = userService.findByUsername(username);
        BusinessServiceDto businessServiceDto = businessServiceService.getByIdOrSlug(serviceId);
        businessService.ensureBusinessBelongsToUser(businessServiceDto.getBusinessId(), userDto.getId());
        businessServiceService.deleteBusinessService(businessServiceDto.getId());

    }
}
