package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TenantContextResolver {
    UserDto currentUser(HttpServletRequest httpServletRequest);

    String currentUserId(HttpServletRequest httpServletRequest);

    List<String> ownedBusinessIds(HttpServletRequest httpServletRequest);

    void ensureBusinessOwnership(HttpServletRequest httpServletRequest, String businessId);
}
