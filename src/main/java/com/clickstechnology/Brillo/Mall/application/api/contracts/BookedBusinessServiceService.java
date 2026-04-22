package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.BookAServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBookingRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface BookedBusinessServiceService {
    BookedServiceDto book(BookAServiceRequest bookAServiceRequest, UserDto userDto, BusinessServiceDto businessService, CustomerDto customer);

    PaginatedResponse<BookedServiceDto> getBookingsForBusinessService(String serviceId, Pageable pageable);

    PaginatedResponse<BookedServiceDto> getBookingsForBusiness(String businessId, Pageable pageable);

    PaginatedResponse<BookedServiceDto> findAllForBusinesses(Set<String> businessIds, Pageable pageable);

    PaginatedResponse<BookedServiceDto> findAllForUser(String userId, Pageable pageable);

    BookedServiceDto findById(String bookingId);

    void ensureBookingBelongsToUser(BookedServiceDto bookedService, String userId);

    void cancel(String bookingId);

    BookedServiceDto updateBooking(String bookingId, UpdateBookingRequest updateBookingRequest);

    void delete(String bookingId);
}
