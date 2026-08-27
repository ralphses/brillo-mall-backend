package com.clickstechnology.Brillo.Mall.application.features.support;

import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportAccessServiceTest {

    @Mock
    private TenantContextResolver tenantContextResolver;

    @Mock
    private HttpServletRequest request;

    private SupportAccessService supportAccessService;

    @BeforeEach
    void setUp() {
        supportAccessService = new SupportAccessService(tenantContextResolver);
    }

    @Test
    void requireSupportUser_allowsAgent() {
        UserDto user = UserDto.builder().id("10").roles(List.of("AGENT")).build();
        when(tenantContextResolver.currentUser(request)).thenReturn(user);

        UserDto result = supportAccessService.requireSupportUser(request);

        assertThat(result.getId()).isEqualTo("10");
        assertThat(supportAccessService.isAgent(result)).isTrue();
        assertThat(supportAccessService.isSuperAdmin(result)).isFalse();
    }

    @Test
    void requireSupportUser_allowsSuperAdmin() {
        UserDto user = UserDto.builder().id("11").roles(List.of("SUPER_ADMIN")).build();
        when(tenantContextResolver.currentUser(request)).thenReturn(user);

        UserDto result = supportAccessService.requireSupportUser(request);

        assertThat(result.getId()).isEqualTo("11");
        assertThat(supportAccessService.isSuperAdmin(result)).isTrue();
    }

    @Test
    void requireSupportUser_rejectsMerchantAdminWithoutSupportRole() {
        UserDto user = UserDto.builder().id("12").roles(List.of("ADMIN")).build();
        when(tenantContextResolver.currentUser(request)).thenReturn(user);

        assertThatThrownBy(() -> supportAccessService.requireSupportUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("support operations");
    }
}
