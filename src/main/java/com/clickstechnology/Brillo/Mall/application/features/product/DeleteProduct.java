package com.clickstechnology.Brillo.Mall.application.features.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteProduct {

    private final ProductService productService;
    private final UserService userService;
    private final BusinessService businessService;
    private final AuthenticationUtil authenticationUtil;
    private final HttpServletRequest request;

    public void execute(String businessId, String productId) {

        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(request);
        UserDto user = userService.findByUsername(authenticatedUsername);
        businessService.ensureBusinessBelongsToUser(businessId, user.getId());

        BusinessDto business = businessService.findByBusinessId(businessId);
        if (business.getStatus() != EntityStatus.ACTIVE) {
            log.warn(":::Failed to delete product: Business {} is not active.", businessId);
            throw new BusinessException("Business is not active. Kindly verify or contact admin for support.");
        }

        productService.deleteProduct(productId);
    }
}
