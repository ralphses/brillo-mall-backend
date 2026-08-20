package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.api.contracts.CategoryCatalogService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PublicSearchIndexSync;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessServiceServiceImplTest {

    @Mock
    private BusinessServiceRepository businessServiceRepository;

    @Mock
    private CategoryCatalogService categoryCatalogService;

    @Mock
    private BusinessServiceSpecification businessServiceSpecification;

    @Mock
    private PublicSearchIndexSync publicSearchIndexSync;

    @InjectMocks
    private BusinessServiceServiceImpl service;

    @Test
    void addService_shouldPersistResolvedCategory() {
        AddBusinessServiceRequest request = new AddBusinessServiceRequest(
                "business-1",
                "Home Visit",
                "Support at home",
                "Pharmacy",
                PricingType.FIXED,
                BigDecimal.valueOf(5000),
                30,
                true,
                false
        );
        UserDto userDto = UserDto.builder().id("user-1").build();

        when(categoryCatalogService.resolveCategory(BusinessCategory.SERVICES, request.getCategory()))
                .thenReturn("PHARMACY");
        when(businessServiceRepository.existsByBusinessIdAndSlug("business-1", "home-visit"))
                .thenReturn(false);
        when(businessServiceRepository.save(any(BusinessService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessServiceDto result = service.addService(request, userDto);

        assertThat(result.getCategory()).isEqualTo("PHARMACY");
        ArgumentCaptor<BusinessService> captor = ArgumentCaptor.forClass(BusinessService.class);
        org.mockito.Mockito.verify(businessServiceRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo("PHARMACY");
        org.mockito.Mockito.verify(publicSearchIndexSync).syncBusinessService(captor.getValue());
    }

    @Test
    void updateService_shouldPersistResolvedCategory() {
        BusinessService existing = BusinessService.builder()
                .businessId("business-1")
                .name("Home Visit")
                .slug("home-visit")
                .category("OLD")
                .pricingType(PricingType.FIXED)
                .basePrice(BigDecimal.valueOf(5000))
                .durationMinutes(30)
                .build();
        existing.setReference("service-1");

        UpdateBusinessServiceRequest request = new UpdateBusinessServiceRequest();
        request.setCategory("Pharmacy");

        when(categoryCatalogService.resolveCategory(BusinessCategory.SERVICES, request.getCategory()))
                .thenReturn("PHARMACY");
        when(businessServiceRepository.findByServiceId("service-1"))
                .thenReturn(Optional.of(existing));
        when(businessServiceRepository.save(any(BusinessService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessServiceDto result = service.updateService("service-1", request, UserDto.builder().id("user-1").build());

        assertThat(result.getCategory()).isEqualTo("PHARMACY");
        assertThat(existing.getCategory()).isEqualTo("PHARMACY");
        org.mockito.Mockito.verify(publicSearchIndexSync).syncBusinessService(existing);
    }

    @Test
    void listServices_shouldApplyPublicFilters() {
        BusinessService existing = BusinessService.builder()
                .businessId("business-1")
                .name("Home Delivery")
                .slug("home-delivery")
                .category("PHARMACY")
                .pricingType(PricingType.FIXED)
                .basePrice(BigDecimal.valueOf(5000))
                .durationMinutes(30)
                .negotiable(true)
                .requiresSchedule(false)
                .active(true)
                .status(EntityStatus.ACTIVE)
                .build();
        existing.setReference("service-1");

        when(businessServiceSpecification.getServices(
                eq("business-1"),
                eq("delivery"),
                eq("PHARMACY"),
                eq(PricingType.FIXED),
                eq(true),
                eq(false),
                eq(EntityStatus.ACTIVE),
                eq(Boolean.TRUE)))
                .thenReturn((root, query, cb) -> cb.conjunction());
        when(businessServiceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(existing), PageRequest.of(0, 20), 1));

        var result = service.listServices(
                "business-1",
                1,
                20,
                "delivery",
                "PHARMACY",
                PricingType.FIXED,
                true,
                false,
                EntityStatus.ACTIVE);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getSlug()).isEqualTo("home-delivery");
    }

    @Test
    void deleteBusinessService_shouldMarkDeletedAndSync() {
        BusinessService existing = BusinessService.builder()
                .businessId("business-1")
                .name("Home Delivery")
                .slug("home-delivery")
                .status(EntityStatus.ACTIVE)
                .build();
        existing.setReference("service-1");

        when(businessServiceRepository.findByServiceId("service-1"))
                .thenReturn(Optional.of(existing));
        when(businessServiceRepository.save(any(BusinessService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteBusinessService("service-1");

        assertThat(existing.getStatus()).isEqualTo(EntityStatus.DELETED);
        org.mockito.Mockito.verify(publicSearchIndexSync).syncBusinessService(existing);
    }
}
