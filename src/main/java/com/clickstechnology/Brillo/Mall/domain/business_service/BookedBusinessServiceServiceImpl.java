package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BookedBusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.BookAServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBookingRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BookingStatus;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.features.notifications.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class BookedBusinessServiceServiceImpl implements BookedBusinessServiceService {
    private final BookedBusinessServiceRepository bookedBusinessServiceRepository;
    private final BusinessServiceRequestService businessServiceRequestService;
    private final BusinessService businessService;
    private final CustomerService customerService;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    @Transactional
    public BookedServiceDto book(
            BookAServiceRequest bookAServiceRequest,
            UserDto userDto,
            BusinessServiceDto businessService,
            CustomerDto customer) {

        if (businessService.getBasePrice() == null && bookAServiceRequest.getTotalPrice() == null) {
            throw new BusinessException("A booking amount is required.");
        }

        if (businessService.isRequiresSchedule() && bookAServiceRequest.getScheduledDate() == null) {
            throw new BusinessException("This service requires a scheduled date.");
        }

        if (StringUtils.hasText(bookAServiceRequest.getServiceRequestId())
                && bookedBusinessServiceRepository.findByServiceRequestId(bookAServiceRequest.getServiceRequestId()).isPresent()) {
            throw new BusinessException("A booking already exists for this service request.");
        }

        BookedBusinessService bookedBusinessService = BookedBusinessService.builder()
                .userId(userDto.getId())
                .businessServiceId(businessService.getId())
                .customerId(customer.getId())
                .businessId(businessService.getBusinessId())
                .agreedPrice(resolveBookingAmount(bookAServiceRequest, businessService))
                .serviceRequestId(StringUtils.hasText(bookAServiceRequest.getServiceRequestId())
                        ? bookAServiceRequest.getServiceRequestId()
                        : null)
                .location(bookAServiceRequest.getLocation())
                .bookingStatus(BookingStatus.PENDING)
                .scheduledDate(bookAServiceRequest.getScheduledDate())
                .status(EntityStatus.ACTIVE)
                .build();

        BookedBusinessService saved = bookedBusinessServiceRepository.save(bookedBusinessService);

        if (StringUtils.hasText(saved.getServiceRequestId())) {
            businessServiceRequestService.markBooked(saved.getServiceRequestId());
        }

        BookedServiceDto bookingDto = enrich(saved);
        if (notificationEventPublisher != null) {
            notificationEventPublisher.publishBookingCreated(
                    bookingDto,
                    resolveCustomer(customer.getId()),
                    resolveBusiness(businessService.getBusinessId())
            );
        }
        return bookingDto;
    }

    private java.math.BigDecimal resolveBookingAmount(BookAServiceRequest bookAServiceRequest, BusinessServiceDto businessService) {
        if (bookAServiceRequest.getTotalPrice() != null) {
            return bookAServiceRequest.getTotalPrice();
        }
        return businessService.getBasePrice();
    }

    @Override
    public PaginatedResponse<BookedServiceDto> getBookingsForBusinessService(
            String serviceId,
            BookingStatus bookingStatus,
            Pageable pageable) {
        Page<BookedBusinessService> businesses = bookingStatus == null
                ? bookedBusinessServiceRepository.findAllByBusinessServiceId(serviceId, pageable)
                : bookedBusinessServiceRepository.findAllByBusinessServiceIdAndBookingStatus(serviceId, bookingStatus, pageable);

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
    public PaginatedResponse<BookedServiceDto> getBookingsForBusiness(
            String businessId,
            BookingStatus bookingStatus,
            Pageable pageable) {
        Page<BookedBusinessService> businesses = bookingStatus == null
                ? bookedBusinessServiceRepository.findAllByBusinessId(businessId, pageable)
                : bookedBusinessServiceRepository.findAllByBusinessIdAndBookingStatus(businessId, bookingStatus, pageable);

        return buildPaginatedResponse(businesses, pageable);
    }

    @Override
    public PaginatedResponse<BookedServiceDto> findAllForBusinesses(
            Set<String> businessIds,
            BookingStatus bookingStatus,
            Pageable pageable) {
        Page<BookedBusinessService> businesses = bookingStatus == null
                ? bookedBusinessServiceRepository.findAllByBusinessIdIn(businessIds, pageable)
                : bookedBusinessServiceRepository.findAllByBusinessIdInAndBookingStatus(businessIds, bookingStatus, pageable);

        return buildPaginatedResponse(businesses, pageable);
    }

    @Override
    public PaginatedResponse<BookedServiceDto> findAllForUser(
            String userId,
            BookingStatus bookingStatus,
            Pageable pageable) {
        Page<BookedBusinessService> businesses = bookingStatus == null
                ? bookedBusinessServiceRepository.findAllByUserId(userId, pageable)
                : bookedBusinessServiceRepository.findAllByUserIdAndBookingStatus(userId, bookingStatus, pageable);

        return buildPaginatedResponse(businesses, pageable);
    }

    private BookedBusinessService findByReference(String reference) {
        return bookedBusinessServiceRepository.findByReference(reference)
                .orElseThrow(() -> new BusinessException("Booking not found"));
    }

    @Override
    public BookedServiceDto findById(String bookingId) {
        return enrich(findByReference(bookingId));
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
        if (bookedBusinessService.getBookingStatus() == BookingStatus.CANCELLED) {
            return;
        }
        BookingStatus previousStatus = bookedBusinessService.getBookingStatus();
        ensureCancelable(bookedBusinessService);
        bookedBusinessService.setStatus(EntityStatus.INACTIVE);
        bookedBusinessService.setBookingStatus(BookingStatus.CANCELLED);
        BookedBusinessService saved = bookedBusinessServiceRepository.save(bookedBusinessService);
        if (notificationEventPublisher != null) {
            notificationEventPublisher.publishBookingStatusChanged(
                    enrich(saved),
                    resolveCustomer(saved.getCustomerId()),
                    resolveBusiness(saved.getBusinessId()),
                    previousStatus,
                    BookingStatus.CANCELLED
            );
        }
    }

    @Override
    @Transactional
    public BookedServiceDto updateBooking(String bookingId, UpdateBookingRequest updateBookingRequest) {
        BookedBusinessService bookedBusinessService = findByReference(bookingId);
        BookingStatus previousStatus = bookedBusinessService.getBookingStatus();
        ensureMutable(bookedBusinessService);

        if (updateBookingRequest.getAgreedPrice() != null) {
            if (bookedBusinessService.getBookingStatus() != BookingStatus.PENDING) {
                throw new BusinessException("Booking price can only be changed while pending.");
            }
            bookedBusinessService.setAgreedPrice(updateBookingRequest.getAgreedPrice());
        }

        if (updateBookingRequest.getScheduledDate() != null) {
            if (bookedBusinessService.getBookingStatus() == BookingStatus.COMPLETED
                    || bookedBusinessService.getBookingStatus() == BookingStatus.CANCELLED) {
                throw new BusinessException("Terminal bookings cannot be rescheduled.");
            }
            bookedBusinessService.setScheduledDate(updateBookingRequest.getScheduledDate());
        }

        if (updateBookingRequest.getLocation() != null) {
            if (bookedBusinessService.getBookingStatus() == BookingStatus.COMPLETED
                    || bookedBusinessService.getBookingStatus() == BookingStatus.CANCELLED) {
                throw new BusinessException("Terminal bookings cannot be relocated.");
            }
            bookedBusinessService.setLocation(updateBookingRequest.getLocation());
        }

        if (updateBookingRequest.getStatus() != null) {
            if (bookedBusinessService.getBookingStatus() == updateBookingRequest.getStatus()) {
                return bookedBusinessService.dto();
            }
            validateTransition(bookedBusinessService.getBookingStatus(), updateBookingRequest.getStatus());
            bookedBusinessService.setBookingStatus(updateBookingRequest.getStatus());

            if (updateBookingRequest.getStatus() == BookingStatus.CANCELLED) {
                bookedBusinessService.setStatus(EntityStatus.INACTIVE);
            }
        }

        BookedBusinessService saved = bookedBusinessServiceRepository.save(bookedBusinessService);
        if (previousStatus != saved.getBookingStatus()) {
            if (notificationEventPublisher != null) {
                notificationEventPublisher.publishBookingStatusChanged(
                        enrich(saved),
                        resolveCustomer(saved.getCustomerId()),
                        resolveBusiness(saved.getBusinessId()),
                        previousStatus,
                        saved.getBookingStatus()
                );
            }
        }
        return enrich(saved);
    }

    private void ensureMutable(BookedBusinessService bookedBusinessService) {
        if (bookedBusinessService.getBookingStatus() == BookingStatus.COMPLETED
                || bookedBusinessService.getBookingStatus() == BookingStatus.CANCELLED
                || bookedBusinessService.getBookingStatus() == BookingStatus.DELETED) {
            throw new BusinessException("Booking is already completed or closed.");
        }
    }

    private void ensureCancelable(BookedBusinessService bookedBusinessService) {
        if (bookedBusinessService.getBookingStatus() != BookingStatus.PENDING
                && bookedBusinessService.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Booking cannot be cancelled in its current state.");
        }
    }

    private void validateTransition(BookingStatus currentStatus, BookingStatus nextStatus) {
        boolean validTransition = switch (currentStatus) {
            case PENDING -> nextStatus == BookingStatus.CONFIRMED || nextStatus == BookingStatus.CANCELLED;
            case CONFIRMED -> nextStatus == BookingStatus.IN_PROGRESS || nextStatus == BookingStatus.CANCELLED;
            case IN_PROGRESS -> nextStatus == BookingStatus.COMPLETED;
            default -> false;
        };

        if (!validTransition) {
            throw new BusinessException("Invalid booking status transition.");
        }
    }

    @Override
    @Transactional
    public void delete(String bookingId) {
        BookedBusinessService bookedBusinessService = findByReference(bookingId);
        if (bookedBusinessService.getBookingStatus() == BookingStatus.DELETED) {
            return;
        }
        BookingStatus previousStatus = bookedBusinessService.getBookingStatus();
        bookedBusinessService.setStatus(EntityStatus.DELETED);
        bookedBusinessService.setBookingStatus(BookingStatus.DELETED);
        BookedBusinessService saved = bookedBusinessServiceRepository.save(bookedBusinessService);
        if (notificationEventPublisher != null) {
            notificationEventPublisher.publishBookingStatusChanged(
                    enrich(saved),
                    resolveCustomer(saved.getCustomerId()),
                    resolveBusiness(saved.getBusinessId()),
                    previousStatus,
                    BookingStatus.DELETED
            );
        }
    }

    private BookedServiceDto enrich(BookedBusinessService booking) {
        BookedServiceDto dto = booking.dto();
        dto.setCustomer(resolveCustomer(booking.getCustomerId()));
        dto.setBusiness(resolveBusiness(booking.getBusinessId()));
        return dto;
    }

    private CustomerDto resolveCustomer(String customerId) {
        if (customerService == null) {
            return CustomerDto.builder().id(customerId).build();
        }
        try {
            return customerService.findById(customerId);
        } catch (Exception ex) {
            return CustomerDto.builder().id(customerId).build();
        }
    }

    private BusinessDto resolveBusiness(String businessId) {
        if (this.businessService == null) {
            return BusinessDto.builder().id(businessId).build();
        }
        try {
            return this.businessService.findByBusinessId(businessId);
        } catch (Exception ex) {
            return BusinessDto.builder().id(businessId).build();
        }
    }
}
