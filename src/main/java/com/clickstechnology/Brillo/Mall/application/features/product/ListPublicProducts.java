package com.clickstechnology.Brillo.Mall.application.features.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListPublicProducts {

    private final ProductService productService;

    public PaginatedResponse<ProductDto> execute(
            String businessId,
            Integer page,
            Integer pageSize,
            String q,
            String category,
            Boolean flashSale,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStockOnly) {
        log.info(":::Loading public products for q: {}, category: {}, businessId: {}", q, category, businessId);
        return productService.getProducts(
                businessId,
                page,
                pageSize,
                q,
                category,
                flashSale,
                minPrice,
                maxPrice,
                inStockOnly,
                EntityStatus.ACTIVE);
    }
}
