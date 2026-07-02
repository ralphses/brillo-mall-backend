package com.clickstechnology.Brillo.Mall.infrastructure.authentication;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultTenantContextResolver implements TenantContextResolver {

    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;
    private final BusinessService businessService;

    @Override
    public UserDto currentUser(HttpServletRequest httpServletRequest) {
        String username = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        return userService.findByUsername(username);
    }

    @Override
    public String currentUserId(HttpServletRequest httpServletRequest) {
        return currentUser(httpServletRequest).getId();
    }

    @Override
    public List<String> ownedBusinessIds(HttpServletRequest httpServletRequest) {
        return businessService.findAllByOwnerId(currentUserId(httpServletRequest))
                .stream()
                .map(BusinessDto::getId)
                .toList();
    }

    @Override
    public void ensureBusinessOwnership(HttpServletRequest httpServletRequest, String businessId) {
        businessService.ensureBusinessBelongsToUser(businessId, currentUserId(httpServletRequest));
    }
}
