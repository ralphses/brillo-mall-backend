package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.BookAServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBookingRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BookingStatus;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface BookedBusinessServiceService {
    BookedServiceDto book(BookAServiceRequest bookAServiceRequest, UserDto userDto, BusinessServiceDto businessService, CustomerDto customer);

    PaginatedResponse<BookedServiceDto> getBookingsForBusinessService(
            String serviceId,
            BookingStatus bookingStatus,
            Pageable pageable);

    PaginatedResponse<BookedServiceDto> getBookingsForBusiness(
            String businessId,
            BookingStatus bookingStatus,
            Pageable pageable);

    PaginatedResponse<BookedServiceDto> findAllForBusinesses(
            Set<String> businessIds,
            BookingStatus bookingStatus,
            Pageable pageable);

    PaginatedResponse<BookedServiceDto> findAllForUser(
            String userId,
            BookingStatus bookingStatus,
            Pageable pageable);

    BookedServiceDto findById(String bookingId);

    void ensureBookingBelongsToUser(BookedServiceDto bookedService, String userId);

    void cancel(String bookingId);

    BookedServiceDto updateBooking(String bookingId, UpdateBookingRequest updateBookingRequest);

    void delete(String bookingId);
}
