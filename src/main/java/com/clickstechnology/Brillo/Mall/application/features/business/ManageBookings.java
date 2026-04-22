package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BookedBusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBookingRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageBookings {

    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;
    private final BusinessService businessService;
    private final BusinessServiceService businessServiceService;
    private final BookedBusinessServiceService bookedBusinessServiceService;
    private final CustomerService customerService;

    public PaginatedResponse<BookedServiceDto> getBookings(
            final String businessId,
            final String businessServiceId,
            final boolean isBusiness,
            final Integer page,
            final Integer pageSize,
            final HttpServletRequest httpServletRequest) {

        UserDto user = getAuthenticatedUser(httpServletRequest);
        Pageable pageable = AppUtils.getPageable(page, pageSize);

        if (isBusiness && user.getRoles().contains(UserRole.ADMIN.name())) {
            return getBookingsForBusinessAdmin(businessId, businessServiceId, user, pageable);
        }

        return bookedBusinessServiceService.findAllForUser(user.getId(), pageable);
    }

    public BookedServiceDto getBooking(String bookingId, HttpServletRequest httpServletRequest) {
        UserDto user = getAuthenticatedUser(httpServletRequest);
        BookedServiceDto bookedService = bookedBusinessServiceService.findById(bookingId);
        authorizeUserForBooking(user, bookedService);
        return bookedService;
    }

    @LoggableRequest
    public void cancelBooking(String bookingId, HttpServletRequest httpServletRequest) {
        UserDto user = getAuthenticatedUser(httpServletRequest);
        BookedServiceDto bookedService = bookedBusinessServiceService.findById(bookingId);
        authorizeUserForBooking(user, bookedService);
        bookedBusinessServiceService.cancel(bookingId);
    }

    @LoggableRequest
    public BookedServiceDto updateBooking(
            final String bookingId,
            final UpdateBookingRequest updateBookingRequest,
            final HttpServletRequest httpServletRequest) {
        UserDto user = getAuthenticatedUser(httpServletRequest);
        BookedServiceDto bookedService = bookedBusinessServiceService.findById(bookingId);
        authorizeUserForBooking(user, bookedService);

        BookedServiceDto updatedBooking = bookedBusinessServiceService.updateBooking(bookingId, updateBookingRequest);

        if (updateBookingRequest.getCustomer() != null) {
            String customerId = updatedBooking.getCustomer().getId();
            customerService.updateCustomer(customerId, updateBookingRequest.getCustomer());
        }

        return updatedBooking;
    }

    @LoggableRequest
    public void deleteBooking(String bookingId, HttpServletRequest httpServletRequest) {
        UserDto user = getAuthenticatedUser(httpServletRequest);
        BookedServiceDto bookedService = bookedBusinessServiceService.findById(bookingId);
        authorizeUserForBooking(user, bookedService);
        bookedBusinessServiceService.delete(bookingId);
    }

    private UserDto getAuthenticatedUser(HttpServletRequest httpServletRequest) {
        String username = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        return userService.findByUsername(username);
    }

    private PaginatedResponse<BookedServiceDto> getBookingsForBusinessAdmin(
            String businessId, String businessServiceId, UserDto user, Pageable pageable) {
        if (Objects.nonNull(businessServiceId)) {
            BusinessServiceDto thisBusinessService = businessServiceService.getByIdOrSlug(businessServiceId);
            businessService.ensureBusinessBelongsToUser(thisBusinessService.getBusinessId(), user.getId());
            return bookedBusinessServiceService.getBookingsForBusinessService(thisBusinessService.getId(), pageable);
        }

        if (Objects.nonNull(businessId)) {
            BusinessDto business = businessService.findByBusinessId(businessId);
            businessService.ensureBusinessBelongsToUser(business.getId(), user.getId());
            return bookedBusinessServiceService.getBookingsForBusiness(business.getId(), pageable);
        }

        List<BusinessDto> adminBusinesses = businessService.findAllByOwnerId(user.getId());
        Set<String> businessIds = adminBusinesses.stream().map(BusinessDto::getId).collect(Collectors.toSet());

        return bookedBusinessServiceService.findAllForBusinesses(businessIds, pageable);
    }

    private void authorizeUserForBooking(UserDto user, BookedServiceDto bookedService) {
        List<String> userRoles = user.getRoles();
        if (userRoles.contains(UserRole.ADMIN.name())) {
            businessService.ensureBusinessBelongsToUser(bookedService.getBusiness().getId(), user.getId());
        } else if (userRoles.contains(UserRole.USER.name())) {
            bookedBusinessServiceService.ensureBookingBelongsToUser(bookedService, user.getId());
        } else {
            throw new BusinessException("Booking not found or invalid");
        }
    }
}