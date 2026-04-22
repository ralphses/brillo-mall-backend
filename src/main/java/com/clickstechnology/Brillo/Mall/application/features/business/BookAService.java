package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BookedBusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.BookAServiceRequest;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookAService {

    private final BookedBusinessServiceService bookedBusinessServiceService;
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;
    private final BusinessServiceService businessServiceService;
    private final BusinessServiceRequestService businessServiceRequestService;
    private final CustomerService customerService;

    @LoggableRequest
    public BookedServiceDto execute(
            final BookAServiceRequest bookAServiceRequest, final HttpServletRequest httpServletRequest) {

        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto userDto = userService.findByUsername(authenticatedUsername);

        // validate service id.
        BusinessServiceDto businessService = businessServiceService.findById(bookAServiceRequest.getServiceId());

        businessServiceService.validatePriceAgreed(businessService, bookAServiceRequest.getTotalPrice());

        if (!bookAServiceRequest.getServiceRequestId().isBlank() && !bookAServiceRequest.isHuman()) {
            BusinessServiceRequestDto serviceRequest = businessServiceRequestService.findById(bookAServiceRequest.getServiceRequestId());
            businessServiceRequestService.ensureBelongsToUser(serviceRequest, userDto.getId());
            businessServiceRequestService.ensureBelongsToService(serviceRequest, businessService.getId());
        }

        CustomerDto customer = customerService.resolveCustomer(bookAServiceRequest.getCustomer(), userDto.getId(), Set.of(businessService.getBusinessId()));

        return bookedBusinessServiceService.book(bookAServiceRequest, userDto, businessService, customer);
    }
}
