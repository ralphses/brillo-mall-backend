package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BusinessServicePublicListingIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService businessService;

    @Autowired
    private com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService businessServiceService;

    @Autowired
    private BusinessServiceRepository businessServiceRepository;

    @MockitoBean
    private CacheUtil cacheUtil;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void listServices_shouldReturnOnlyVisibleMatches() {
        UserDto owner = registerOwner();
        BusinessDto business = onboardBusiness(owner, "Public Services Store");

        createService(business.getId(), "Home Delivery", "Fast delivery", "Pharmacy", PricingType.FIXED, true, false);
        BusinessServiceDto hiddenService = createService(business.getId(), "Home Delivery Plus", "Express delivery", "Pharmacy", PricingType.FIXED, true, false);
        BusinessService hiddenEntity = businessServiceRepository.findByServiceId(hiddenService.getId()).orElseThrow();
        hiddenEntity.setActive(false);
        businessServiceRepository.save(hiddenEntity);
        entityManager.flush();

        BusinessServiceDto deletedService = createService(business.getId(), "Delivery Deluxe", "Premium delivery", "Pharmacy", PricingType.FIXED, true, false);
        BusinessService deletedEntity = businessServiceRepository.findByServiceId(deletedService.getId()).orElseThrow();
        deletedEntity.setStatus(EntityStatus.DELETED);
        businessServiceRepository.save(deletedEntity);
        entityManager.flush();

        var result = businessServiceService.listServices(
                business.getId(),
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
        assertThat(result.getItems().get(0).getName()).isEqualTo("Home Delivery");
        assertThat(result.getItems().get(0).getCategory()).isEqualTo("PHARMACY");
        assertThat(result.getItems().get(0).getPricingType()).isEqualTo(PricingType.FIXED);
        assertThat(result.getItems().get(0).isNegotiable()).isTrue();
        assertThat(result.getItems().get(0).isRequiresSchedule()).isFalse();
        assertThat(result.getItems().get(0).isActive()).isTrue();
    }

    @Test
    void listServices_shouldReturnEmptyPageWhenNoMatches() {
        UserDto owner = registerOwner();
        BusinessDto business = onboardBusiness(owner, "Empty Services Store");
        createService(business.getId(), "Laundry", "Laundry service", "Beauty", PricingType.NEGOTIABLE, false, true);

        var result = businessServiceService.listServices(
                business.getId(),
                1,
                20,
                "does-not-exist",
                "PHARMACY",
                PricingType.FIXED,
                true,
                false,
                EntityStatus.ACTIVE);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getItems()).isEmpty();
    }

    private UserDto registerOwner() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Service Owner",
                "07036008888",
                "Password123",
                null
        );
        userService.registerNewUser(registerRequest, null, false);
        entityManager.flush();
        return userService.findByUsername("07036008888");
    }

    private BusinessDto onboardBusiness(UserDto owner, String businessName) {
        OnboardBusinessRequest onboardBusinessRequest = new OnboardBusinessRequest();
        onboardBusinessRequest.setBusinessName(businessName);
        businessService.createNew(onboardBusinessRequest, owner, "logo.png", BusinessCategory.SERVICES);
        entityManager.flush();
        return businessService.findByBusinessSlug(com.clickstechnology.Brillo.Mall.application.utils.AppUtils.generateSlug(businessName));
    }

    private BusinessServiceDto createService(
            String businessId,
            String name,
            String description,
            String category,
            PricingType pricingType,
            boolean negotiable,
            boolean requiresSchedule) {
        AddBusinessServiceRequest request = new AddBusinessServiceRequest();
        request.setBusinessId(businessId);
        request.setName(name);
        request.setDescription(description);
        request.setCategory(category);
        request.setPricingType(pricingType);
        request.setBasePrice(BigDecimal.valueOf(5000));
        request.setDurationMinutes(30);
        request.setNegotiable(negotiable);
        request.setRequiresSchedule(requiresSchedule);

        BusinessServiceDto dto = businessServiceService.addService(request, UserDto.builder().id("user-id").build());
        entityManager.flush();
        return dto;
    }
}
