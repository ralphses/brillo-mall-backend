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
import com.clickstechnology.Brillo.Mall.application.enums.ServiceRequestStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
            final BookAServiceRequest bookAServiceRequest,
            final HttpServletRequest httpServletRequest) {

        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto userDto = userService.findByUsername(authenticatedUsername);
        BigDecimal originalRequestedPrice = bookAServiceRequest.getTotalPrice();

        BusinessServiceDto businessService = businessServiceService.findById(bookAServiceRequest.getServiceId());
        businessServiceService.validateForBooking(businessService);

        BigDecimal resolvedPrice = resolveBookingPrice(bookAServiceRequest, businessService);
        bookAServiceRequest.setTotalPrice(resolvedPrice);

        if (!bookAServiceRequest.isHuman()
                && originalRequestedPrice != null
                && originalRequestedPrice.compareTo(resolvedPrice) != 0) {
            throw new BusinessException("Booking amount does not match the server-resolved amount.");
        }

        if (StringUtils.hasText(bookAServiceRequest.getServiceRequestId())) {
            BusinessServiceRequestDto serviceRequest = businessServiceRequestService.findById(bookAServiceRequest.getServiceRequestId());
            businessServiceRequestService.ensureBelongsToUser(serviceRequest, userDto.getId());
            businessServiceRequestService.ensureBelongsToService(serviceRequest, businessService.getId());

            if (!bookAServiceRequest.isHuman() && serviceRequest.getRequestStatus() != ServiceRequestStatus.AGREED) {
                throw new BusinessException("Service request must be agreed before booking.");
            }

            if (!bookAServiceRequest.isHuman()
                    && serviceRequest.getAgreedPrice() != null
                    && serviceRequest.getAgreedPrice().compareTo(resolvedPrice) != 0) {
                throw new BusinessException("Booking amount does not match the agreed service request amount.");
            }
        }

        CustomerDto customer = customerService.resolveCustomer(
                bookAServiceRequest.getCustomer(),
                userDto.getId(),
                Set.of(businessService.getBusinessId()));

        return bookedBusinessServiceService.book(bookAServiceRequest, userDto, businessService, customer);
    }

    private BigDecimal resolveBookingPrice(BookAServiceRequest bookAServiceRequest, BusinessServiceDto businessService) {
        if (!StringUtils.hasText(bookAServiceRequest.getServiceRequestId())) {
            if (bookAServiceRequest.isHuman()) {
                if (bookAServiceRequest.getTotalPrice() == null) {
                    throw new BusinessException("Human bookings require a price.");
                }
                businessServiceService.validatePriceAgreed(businessService, bookAServiceRequest.getTotalPrice());
                return bookAServiceRequest.getTotalPrice();
            }

            if (businessService.getBasePrice() == null) {
                throw new BusinessException("This service has no base price.");
            }
            return businessService.getBasePrice();
        }

        BusinessServiceRequestDto serviceRequest = businessServiceRequestService.findById(bookAServiceRequest.getServiceRequestId());
        if (!bookAServiceRequest.isHuman()) {
            if (serviceRequest.getRequestStatus() != ServiceRequestStatus.AGREED || serviceRequest.getAgreedPrice() == null) {
                throw new BusinessException("Service request must be agreed before booking.");
            }
            return serviceRequest.getAgreedPrice();
        }

        if (bookAServiceRequest.getTotalPrice() != null) {
            businessServiceService.validatePriceAgreed(businessService, bookAServiceRequest.getTotalPrice());
            return bookAServiceRequest.getTotalPrice();
        }

        if (serviceRequest.getAgreedPrice() != null) {
            return serviceRequest.getAgreedPrice();
        }

        throw new BusinessException("A price is required for the booking.");
    }
}
