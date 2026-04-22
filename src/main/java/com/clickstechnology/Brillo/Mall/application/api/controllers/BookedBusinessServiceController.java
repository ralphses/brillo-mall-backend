package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.BookAServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBookingRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.features.business.BookAService;
import com.clickstechnology.Brillo.Mall.application.features.business.ManageBookings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/booked-services")
public class BookedBusinessServiceController {

    private final BookAService bookAService;
    private final ManageBookings manageBookings;

    @PostMapping
    public ResponseWrapper<BookedServiceDto> book(
            @Valid @RequestBody final BookAServiceRequest bookAServiceRequest,
            final HttpServletRequest httpServletRequest) {
        BookedServiceDto response = bookAService.execute(bookAServiceRequest, httpServletRequest);
        return success(response);
    }

    @GetMapping
    public ResponseWrapper<PaginatedResponse<BookedServiceDto>> getBookings(
            final HttpServletRequest httpServletRequest,
            @RequestParam(required = false) final String businessId,
            @RequestParam(required = false) final String businessServiceId,
            @RequestParam(required = false, defaultValue = "false") final boolean isBusiness,
            @RequestParam(defaultValue = "1") final Integer page,
            @RequestParam(defaultValue = "10") final Integer pageSize) {
        PaginatedResponse<BookedServiceDto> response = manageBookings.getBookings(
                businessId, businessServiceId, isBusiness, page, pageSize, httpServletRequest);
        return success(response);
    }

    @GetMapping("{bookingId}")
    public ResponseWrapper<BookedServiceDto> getBooking(
            @PathVariable final String bookingId,
            final HttpServletRequest httpServletRequest) {
        BookedServiceDto response = manageBookings.getBooking(bookingId, httpServletRequest);
        return success(response);
    }

    @PostMapping("{bookingId}/cancel")
    public ResponseWrapper<String> cancelBooking(
            @PathVariable final String bookingId,
            final HttpServletRequest httpServletRequest) {
        manageBookings.cancelBooking(bookingId, httpServletRequest);
        return success("Booking has been cancelled");
    }

    @PutMapping("{bookingId}")
    public ResponseWrapper<BookedServiceDto> updateBooking(
            @PathVariable final String bookingId,
            @Valid @RequestBody final UpdateBookingRequest updateBookingRequest,
            final HttpServletRequest httpServletRequest) {
        BookedServiceDto response = manageBookings.updateBooking(bookingId, updateBookingRequest, httpServletRequest);
        return success(response);
    }

    @DeleteMapping("{bookingId}")
    public ResponseWrapper<String> deleteBooking(
            @PathVariable final String bookingId,
            final HttpServletRequest httpServletRequest) {
        manageBookings.deleteBooking(bookingId, httpServletRequest);
        return success("Booking has been deleted");
    }

}
