package com.clickstechnology.Brillo.Mall.application.features.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListAllProducts {

    private final ProductService productService;

    public PaginatedResponse<ProductDto> execute(
            String businessId,
            Integer page,
            Integer pageSize,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStockOnly,
            EntityStatus status) {
        log.info(":::Attempting to get products for businessId: {}, search: {}", businessId, search);
        return productService.getProducts(businessId, page, pageSize, search, minPrice, maxPrice, inStockOnly, status);
    }
}
