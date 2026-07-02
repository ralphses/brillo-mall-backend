package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceBusinessServiceRequest {

    private final UserService userService;
    private final BusinessServiceRequestService businessServiceRequestService;
    private final BusinessServiceService businessServiceService;
    private final AuthenticationUtil authenticationUtil;
    private final CustomerService customerService;

    @LoggableRequest
    public void execute(PlaceBusinessServiceRequestPayload request, HttpServletRequest httpServletRequest) {
        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        if (authenticatedUsername == null) {
            throw new UnauthorizedUserException();
        }

        UserDto user = userService.findByUsername(authenticatedUsername);

        BusinessServiceDto businessService = businessServiceService.findById(request.getBusinessServiceId());
        businessServiceService.validateForRequests(businessService);

        CustomerDto customer = customerService.resolveCustomer(
                request.getCustomer(),
                user.getId(),
                Set.of(businessService.getBusinessId()));

        businessServiceRequestService.create(request, customer, businessService);
    }
}
