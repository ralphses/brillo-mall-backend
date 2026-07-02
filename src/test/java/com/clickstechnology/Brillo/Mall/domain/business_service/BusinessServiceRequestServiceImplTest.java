package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.enums.ServiceRequestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessServiceRequestServiceImplTest {

    @Mock
    private BusinessServiceRequestRepository businessServiceRequestRepository;

    @InjectMocks
    private BusinessServiceRequestServiceImpl service;

    @Test
    void create_shouldStartInNegotiatingState() {
        CustomerDto customer = CustomerDto.builder()
                .id("customer-ref")
                .userId("user-ref")
                .customerName("Ada")
                .customerPhoneNumber("08000000000")
                .address("Lagos")
                .build();

        BusinessServiceDto businessService = BusinessServiceDto.builder()
                .id("service-ref")
                .businessId("business-ref")
                .build();

        PlaceBusinessServiceRequestPayload payload = new PlaceBusinessServiceRequestPayload(
                "service-ref",
                BigDecimal.valueOf(2400),
                BigDecimal.valueOf(3000),
                null,
                0,
                false,
                "Need it fast",
                "wa-conv-1",
                false,
                customer
        );

        when(businessServiceRequestRepository.save(any(BusinessServiceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(payload, customer, businessService);

        ArgumentCaptor<BusinessServiceRequest> captor = ArgumentCaptor.forClass(BusinessServiceRequest.class);
        org.mockito.Mockito.verify(businessServiceRequestRepository).save(captor.capture());

        BusinessServiceRequest saved = captor.getValue();
        assertThat(saved.getRequestStatus()).isEqualTo(ServiceRequestStatus.NEGOTIATING);
        assertThat(saved.getNegotiationAttempts()).isZero();
        assertThat(saved.getBusinessId()).isEqualTo("business-ref");
        assertThat(saved.getCustomerId()).isEqualTo("customer-ref");
    }

    @Test
    void updateRequest_shouldMarkAgreedWhenPriceIsAccepted() {
        BusinessServiceRequest existing = BusinessServiceRequest.builder()
                .businessId("business-ref")
                .businessServiceId("service-ref")
                .customerId("customer-ref")
                .userId("user-ref")
                .requestStatus(ServiceRequestStatus.NEGOTIATING)
                .build();
        existing.setReference("request-ref");

        when(businessServiceRequestRepository.findByReference("request-ref"))
                .thenReturn(java.util.Optional.of(existing));
        when(businessServiceRequestRepository.save(any(BusinessServiceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlaceBusinessServiceRequestPayload payload = new PlaceBusinessServiceRequestPayload(
                "service-ref",
                BigDecimal.valueOf(2400),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(2800),
                1,
                false,
                "Offer accepted",
                "wa-conv-1",
                false,
                CustomerDto.builder().id("customer-ref").build()
        );

        BusinessServiceRequestDto updated = service.updateRequest("request-ref", payload, true);

        assertThat(updated.getRequestStatus()).isEqualTo(ServiceRequestStatus.AGREED);
        assertThat(updated.getNegotiationAttempts()).isEqualTo(1);
        assertThat(updated.getAgreedPrice()).isEqualByComparingTo("2800");
    }
}
