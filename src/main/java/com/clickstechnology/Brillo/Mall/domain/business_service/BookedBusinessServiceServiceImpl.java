package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BookedBusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.BookAServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBookingRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class BookedBusinessServiceServiceImpl implements BookedBusinessServiceService {
    private final BookedBusinessServiceRepository bookedBusinessServiceRepository;

    @Override
    public BookedServiceDto book(BookAServiceRequest bookAServiceRequest, UserDto userDto, BusinessServiceDto businessService, CustomerDto customer) {
        BookedBusinessService bookedBusinessService = BookedBusinessService.builder()
                .userId(userDto.getId())
                .businessServiceId(businessService.getId())
                .customerId(customer.getId())
                .businessId(businessService.getBusinessId())
                .agreedPrice(bookAServiceRequest.getTotalPrice())
                .serviceRequestId(bookAServiceRequest.getServiceRequestId())
                .status(EntityStatus.PENDING)
                .scheduledDate(bookAServiceRequest.getScheduledDate())
                .build();

        return bookedBusinessServiceRepository.save(bookedBusinessService).dto();
    }

    @Override
    public PaginatedResponse<BookedServiceDto> getBookingsForBusinessService(String serviceId, Pageable pageable) {
        Page<BookedBusinessService> businesses = bookedBusinessServiceRepository.findAllByBusinessServiceId(serviceId, pageable);

        return buildPaginatedResponse(businesses, pageable);

    }

    private PaginatedResponse<BookedServiceDto> buildPaginatedResponse(Page<BookedBusinessService> businessesPage, Pageable pageable) {

        List<BookedServiceDto> items = businessesPage.getContent().stream()
                .map(BookedBusinessService::dto)
                .collect(Collectors.toList());

        return PaginatedResponse.<BookedServiceDto>builder()
                .page(pageable.getPageNumber() + 1)
                .perPage(pageable.getPageSize())
                .total((int) businessesPage.getTotalElements())
                .totalPages(businessesPage.getTotalPages())
                .hasNext(businessesPage.hasNext())
                .hasPrevious(businessesPage.hasPrevious())
                .items(items)
                .build();
    }

    @Override
    public PaginatedResponse<BookedServiceDto> getBookingsForBusiness(String businessId, Pageable pageable) {
        Page<BookedBusinessService> businesses = bookedBusinessServiceRepository.findAllByBusinessId(businessId, pageable);

        return buildPaginatedResponse(businesses, pageable);
    }

    @Override
    public PaginatedResponse<BookedServiceDto> findAllForBusinesses(Set<String> businessIds, Pageable pageable) {
        Page<BookedBusinessService> businesses = bookedBusinessServiceRepository.findAllByBusinessIdIn(businessIds, pageable);

        return buildPaginatedResponse(businesses, pageable);
    }

    @Override
    public PaginatedResponse<BookedServiceDto> findAllForUser(String userId, Pageable pageable) {
        Page<BookedBusinessService> businesses = bookedBusinessServiceRepository.findAllByUserId(userId, pageable);

        return buildPaginatedResponse(businesses, pageable);
    }

    private BookedBusinessService findByReference(String reference) {
        return bookedBusinessServiceRepository.findByReference(reference)
                .orElseThrow(() -> new BusinessException("Booking not found"));
    }

    @Override
    public BookedServiceDto findById(String bookingId) {
        return findByReference(bookingId).dto();
    }

    @Override
    public void ensureBookingBelongsToUser(BookedServiceDto bookedService, String userId) {
        if (!bookedService.getUser().getId().equals(userId)) {
            throw new BusinessException("Booking not found or invalid");
        }
    }

    @Override
    @Transactional
    public void cancel(String bookingId) {
        BookedBusinessService bookedBusinessService = findByReference(bookingId);
        bookedBusinessService.setStatus(EntityStatus.INACTIVE);
        bookedBusinessServiceRepository.save(bookedBusinessService);
    }

    @Override
    @Transactional
    public BookedServiceDto updateBooking(String bookingId, UpdateBookingRequest updateBookingRequest) {
        BookedBusinessService bookedBusinessService = findByReference(bookingId);

        if (updateBookingRequest.getAgreedPrice() != null) {
            bookedBusinessService.setAgreedPrice(updateBookingRequest.getAgreedPrice());
        }

        if (updateBookingRequest.getScheduledDate() != null) {
            bookedBusinessService.setScheduledDate(updateBookingRequest.getScheduledDate());
        }

        if (updateBookingRequest.getStatus() != null) {
            bookedBusinessService.setStatus(updateBookingRequest.getStatus());
        }

        return bookedBusinessServiceRepository.save(bookedBusinessService).dto();
    }

    @Async
    @Override
    @Transactional
    public void delete(String bookingId) {
        BookedBusinessService bookedBusinessService = findByReference(bookingId);
        bookedBusinessService.setStatus(EntityStatus.DELETED);
        bookedBusinessServiceRepository.save(bookedBusinessService);
    }
}
