package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.StorefrontData;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.DashboardData;
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

    public DashboardData getDashboardData(Authentication authentication) {
        UserDto userDto = userService.findByUsername(authentication.getName());
        List<BusinessDto> businesses = businessService.findAllByOwnerId(userDto.getId());

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
        return new StorefrontData(
                businessDto.getCategory(),
                businessDto.getId(),
                businessDto.getEmail(),
                businessDto.getStorefrontName() != null ? businessDto.getStorefrontName() : businessDto.getName(),
                businessDto.getPhoneNumber(),
                businessDto.getAddress(),
                businessDto.getWhatsappNumber(),
                businessDto.getWhatsappType(),
                businessDto.getSetupCompleted(),
                businessDto.getStorefrontActive(),
                businessDto.getDescription(),
                businessDto.getLogoUrl(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
