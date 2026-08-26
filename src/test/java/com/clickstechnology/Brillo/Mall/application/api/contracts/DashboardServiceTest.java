package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.SharedConversationAttributionDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.DashboardData;
import com.clickstechnology.Brillo.Mall.application.features.business.OwnerBusinessViewAssembler;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private BusinessService businessService;

    @Mock
    private OwnerBusinessViewAssembler ownerBusinessViewAssembler;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardData_shouldPopulateCurrentStorefrontAndOtherStores() {
        UserDto user = new UserDto();
        user.setId("user-1");
        user.setUsername("owner@example.com");

        BusinessDto activeBusiness = BusinessDto.builder()
                .id("biz-1")
                .name("Main Store")
                .storefrontName("Main Storefront")
                .category(BusinessCategory.PRODUCTS)
                .status(EntityStatus.ACTIVE)
                .whatsappType(WhatsappType.SHARED)
                .storefrontActive(true)
                .setupCompleted(true)
                .storefrontLink("https://brillo.example/store/main")
                .sharedWhatsappLink("https://wa.me/2348039999999?text=Hi")
                .whatsappEntryMode("SHARED")
                .dedicatedNumberReady(false)
                .sharedWhatsappManagedByBrillo(true)
                .sharedConversationAttribution(SharedConversationAttributionDto.builder()
                        .entryAttributedCustomersCount(4L)
                        .activeSharedConversationsCount(2L)
                        .build())
                .build();

        BusinessDto otherBusiness = BusinessDto.builder()
                .id("biz-2")
                .name("Second Store")
                .storefrontName("Second Storefront")
                .category(BusinessCategory.PRODUCTS)
                .status(EntityStatus.ACTIVE)
                .whatsappType(WhatsappType.DEDICATED)
                .storefrontActive(false)
                .setupCompleted(false)
                .build();

        when(authentication.getName()).thenReturn("owner@example.com");
        when(userService.findByUsername("owner@example.com")).thenReturn(user);
        when(businessService.findAllByOwnerId("user-1")).thenReturn(List.of(activeBusiness, otherBusiness));
        when(ownerBusinessViewAssembler.enrichAll(List.of(activeBusiness, otherBusiness)))
                .thenReturn(List.of(activeBusiness, otherBusiness));

        DashboardData dashboardData = dashboardService.getDashboardData(authentication);

        assertThat(dashboardData.getUser().getId()).isEqualTo("user-1");
        assertThat(dashboardData.getCurrentStorefrontData()).isNotNull();
        assertThat(dashboardData.getCurrentStorefrontData().getId()).isEqualTo("biz-1");
        assertThat(dashboardData.getCurrentStorefrontData().getName()).isEqualTo("Main Storefront");
        assertThat(dashboardData.getCurrentStorefrontData().getStorefrontLink()).isEqualTo("https://brillo.example/store/main");
        assertThat(dashboardData.getCurrentStorefrontData().getSharedWhatsappLink()).isEqualTo("https://wa.me/2348039999999?text=Hi");
        assertThat(dashboardData.getCurrentStorefrontData().getWhatsappEntryMode()).isEqualTo("SHARED");
        assertThat(dashboardData.getCurrentStorefrontData().getSharedConversationAttribution().getEntryAttributedCustomersCount()).isEqualTo(4L);
        assertThat(dashboardData.getOtherStores()).containsExactly("Second Storefront");
    }
}
