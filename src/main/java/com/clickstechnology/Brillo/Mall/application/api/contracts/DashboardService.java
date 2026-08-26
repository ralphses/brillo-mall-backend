package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.StorefrontData;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.DashboardData;
import com.clickstechnology.Brillo.Mall.application.features.business.OwnerBusinessViewAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final BusinessService businessService;
    private final OwnerBusinessViewAssembler ownerBusinessViewAssembler;

    public DashboardData getDashboardData(Authentication authentication) {
        UserDto userDto = userService.findByUsername(authentication.getName());
        List<BusinessDto> businesses = ownerBusinessViewAssembler.enrichAll(
                businessService.findAllByOwnerId(userDto.getId()));

        BusinessDto currentBusiness = businesses.stream()
                .filter(business -> Boolean.TRUE.equals(business.getIsActive()))
                .findFirst()
                .orElseGet(() -> businesses.stream().findFirst().orElse(null));

        List<String> otherStores = businesses.stream()
                .filter(Objects::nonNull)
                .filter(business -> !currentBusiness.getId().equals(business.getId()))
                .map(BusinessDto::getStorefrontName)
                .filter(Objects::nonNull)
                .toList();

        return DashboardData.builder()
                .user(userDto)
                .currentStorefrontData(currentBusiness == null ? null : toStorefrontData(currentBusiness))
                .otherStores(otherStores)
                .build();
    }

    private StorefrontData toStorefrontData(BusinessDto businessDto) {
        StorefrontData storefrontData = new StorefrontData();
        storefrontData.setCategory(businessDto.getCategory());
        storefrontData.setId(businessDto.getId());
        storefrontData.setEmail(businessDto.getEmail());
        storefrontData.setName(businessDto.getStorefrontName() != null ? businessDto.getStorefrontName() : businessDto.getName());
        storefrontData.setPhoneNumber(businessDto.getPhoneNumber());
        storefrontData.setAddress(businessDto.getAddress());
        storefrontData.setWhatsappNumber(businessDto.getWhatsappNumber());
        storefrontData.setWhatsappType(businessDto.getWhatsappType());
        storefrontData.setSetUpCompleted(businessDto.getSetupCompleted());
        storefrontData.setActive(businessDto.getStorefrontActive());
        storefrontData.setDescription(businessDto.getDescription());
        storefrontData.setLogoUrl(businessDto.getLogoUrl());
        storefrontData.setStorefrontLink(businessDto.getStorefrontLink());
        storefrontData.setSharedWhatsappLink(businessDto.getSharedWhatsappLink());
        storefrontData.setWhatsappEntryMode(businessDto.getWhatsappEntryMode());
        storefrontData.setDedicatedNumberReady(businessDto.getDedicatedNumberReady());
        storefrontData.setSharedWhatsappManagedByBrillo(businessDto.getSharedWhatsappManagedByBrillo());
        storefrontData.setSharedConversationAttribution(businessDto.getSharedConversationAttribution());
        storefrontData.setOrders(List.of());
        storefrontData.setTransactions(List.of());
        storefrontData.setMembers(List.of());
        storefrontData.setCustomers(List.of());
        return storefrontData;
    }
}
