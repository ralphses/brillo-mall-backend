package com.clickstechnology.Brillo.Mall.application.features.publicread;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicRead {

    private final ProductService productService;
    private final BusinessService businessService;
    private final BusinessServiceService businessServiceService;

    public ProductDto getProduct(String productId) {
        ProductDto product = resolveProduct(productId);
        BusinessDto business = resolveBusiness(product.getBusinessId(), "Product not found");
        ensurePublicBusiness(business, "Product not found");
        return product;
    }

    public BusinessServiceDto getService(String serviceId) {
        BusinessServiceDto service = resolveService(serviceId);
        BusinessDto business = resolveBusiness(service.getBusinessId(), "Business service not found");
        ensurePublicBusiness(business, "Business service not found");
        return service;
    }

    public BusinessDto getBusiness(String businessId) {
        BusinessDto business = resolveBusiness(businessId, "Business not found");
        ensurePublicBusiness(business, "Business not found");
        return business;
    }

    public BusinessDto getBusinessBySlug(String businessSlug) {
        try {
            BusinessDto business = businessService.findByBusinessSlug(businessSlug);
            ensurePublicBusiness(business, "Business not found");
            return business;
        } catch (BusinessException ex) {
            throw new ResourceNotFoundException("Business not found");
        }
    }

    private ProductDto resolveProduct(String productId) {
        try {
            ProductDto product = productService.findProductByProductId(productId);
            if (product.getStatus() != EntityStatus.ACTIVE) {
                throw new ResourceNotFoundException("Product not found");
            }
            return product;
        } catch (BusinessException ex) {
            throw new ResourceNotFoundException("Product not found");
        }
    }

    private BusinessServiceDto resolveService(String serviceId) {
        try {
            BusinessServiceDto service = businessServiceService.findById(serviceId);
            if (service.getStatus() != EntityStatus.ACTIVE || !service.isActive()) {
                throw new ResourceNotFoundException("Business service not found");
            }
            return service;
        } catch (BusinessException ex) {
            throw new ResourceNotFoundException("Business service not found");
        }
    }

    private BusinessDto resolveBusiness(String businessId, String errorMessage) {
        try {
            return businessService.findByBusinessId(businessId);
        } catch (BusinessException ex) {
            throw new ResourceNotFoundException(errorMessage);
        }
    }

    private void ensurePublicBusiness(BusinessDto business, String errorMessage) {
        boolean visible = business.getStatus() == EntityStatus.ACTIVE
                && Boolean.TRUE.equals(business.getIsActive())
                && Boolean.TRUE.equals(business.getStorefrontActive());

        if (!visible) {
            throw new ResourceNotFoundException(errorMessage);
        }
    }
}
