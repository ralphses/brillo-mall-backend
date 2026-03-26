package com.clickstechnology.Brillo.Mall.application.features.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.UpdateProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProduct {

    private final ProductService productService;
    private final BusinessService businessService;
    private final UserService userService;
    private final AuthenticationUtil authenticationUtil;
    private final HttpServletRequest httpServletRequest;


    public ProductDto execute(String businessId, String productId, UpdateProductRequest request) {

        String authenticatedUsername = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto user = userService.findByUsername(authenticatedUsername);
        businessService.ensureBusinessBelongsToUser(businessId, user.getId());

        BusinessDto business = businessService.findByBusinessId(businessId);
        if (business.getStatus() != EntityStatus.ACTIVE) {
            log.warn(":::Failed to update product: Business {} is not active.", businessId);
            throw new BusinessException("Business is not active. Kindly verify or contact admin for support.");
        }
        // Implementation to be added
        return productService.updateProduct(productId, request);
    }
}
