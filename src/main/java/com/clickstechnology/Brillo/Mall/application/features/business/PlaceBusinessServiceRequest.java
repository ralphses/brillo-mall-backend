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

    public void execute(PlaceBusinessServiceRequestPayload request, HttpServletRequest httpServletRequest) {
        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto user = userService.findByUsername(authenticatedUsername);

        // fetch business service
        BusinessServiceDto businessService = businessServiceService.findById(request.getBusinessServiceId());

        // Resolve customer
        CustomerDto customer = customerService.resolveCustomer(request.getCustomer(), user.getId(), Set.of(businessService.getBusinessId()));

        // Ensure business service is fit for requests
        businessServiceService.validateForRequests(businessService);

        // ensure business service be
        businessServiceRequestService.create(request, customer, businessService);
    }
}
