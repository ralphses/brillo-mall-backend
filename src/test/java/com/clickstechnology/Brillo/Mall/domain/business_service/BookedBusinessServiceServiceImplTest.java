package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.BookAServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBookingRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BookingStatus;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookedBusinessServiceServiceImplTest {

    @Mock
    private BookedBusinessServiceRepository bookedBusinessServiceRepository;

    @InjectMocks
    private BookedBusinessServiceServiceImpl service;

    @Test
    void book_shouldStartInPendingState() {
        UserDto user = UserDto.builder().id("user-ref").build();
        CustomerDto customer = CustomerDto.builder().id("customer-ref").build();
        BusinessServiceDto businessService = BusinessServiceDto.builder()
                .id("service-ref")
                .businessId("business-ref")
                .build();

        BookAServiceRequest request = new BookAServiceRequest(
                null,
                "service-ref",
                "Ikeja",
                false,
                customer,
                BigDecimal.valueOf(5000),
                LocalDateTime.of(2026, 1, 1, 9, 0)
        );

        when(bookedBusinessServiceRepository.save(any(BookedBusinessService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.book(request, user, businessService, customer);

        ArgumentCaptor<BookedBusinessService> captor = ArgumentCaptor.forClass(BookedBusinessService.class);
        org.mockito.Mockito.verify(bookedBusinessServiceRepository).save(captor.capture());

        BookedBusinessService saved = captor.getValue();
        assertThat(saved.getBookingStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(saved.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(saved.getBusinessId()).isEqualTo("business-ref");
        assertThat(saved.getCustomerId()).isEqualTo("customer-ref");
    }

    @Test
    void cancel_shouldMoveBookingToCancelledState() {
        BookedBusinessService existing = BookedBusinessService.builder()
                .businessId("business-ref")
                .businessServiceId("service-ref")
                .customerId("customer-ref")
                .userId("user-ref")
                .bookingStatus(BookingStatus.PENDING)
                .status(EntityStatus.ACTIVE)
                .agreedPrice(BigDecimal.valueOf(5000))
                .build();
        existing.setReference("booking-ref");

        when(bookedBusinessServiceRepository.findByReference("booking-ref"))
                .thenReturn(java.util.Optional.of(existing));
        when(bookedBusinessServiceRepository.save(any(BookedBusinessService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.cancel("booking-ref");

        assertThat(existing.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(existing.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void updateBooking_shouldAllowWorkflowStateChanges() {
        BookedBusinessService existing = BookedBusinessService.builder()
                .businessId("business-ref")
                .businessServiceId("service-ref")
                .customerId("customer-ref")
                .userId("user-ref")
                .bookingStatus(BookingStatus.PENDING)
                .status(EntityStatus.ACTIVE)
                .agreedPrice(BigDecimal.valueOf(5000))
                .build();
        existing.setReference("booking-ref");

        when(bookedBusinessServiceRepository.findByReference("booking-ref"))
                .thenReturn(java.util.Optional.of(existing));
        when(bookedBusinessServiceRepository.save(any(BookedBusinessService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateBookingRequest request = UpdateBookingRequest.builder()
                .status(BookingStatus.CONFIRMED)
                .scheduledDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();

        BookedServiceDto updated = service.updateBooking("booking-ref", request);

        assertThat(updated.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(updated.getScheduledDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
    }
}
