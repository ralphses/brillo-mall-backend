package com.clickstechnology.Brillo.Mall.application.features.support;

import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportAccessService {

    private final TenantContextResolver tenantContextResolver;

    public UserDto requireSupportUser(HttpServletRequest request) {
        UserDto user = tenantContextResolver.currentUser(request);
        if (!hasAnyRole(user, UserRole.AGENT, UserRole.SUPER_ADMIN)) {
            throw new BusinessException("You do not have permission to access support operations.");
        }
        return user;
    }

    public boolean isSuperAdmin(UserDto user) {
        return hasAnyRole(user, UserRole.SUPER_ADMIN);
    }

    public boolean isAgent(UserDto user) {
        return hasAnyRole(user, UserRole.AGENT);
    }

    private boolean hasAnyRole(UserDto user, UserRole... roles) {
        List<String> userRoles = user.getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        for (UserRole role : roles) {
            if (userRoles.contains(role.name())) {
                return true;
            }
        }
        return false;
    }
}
