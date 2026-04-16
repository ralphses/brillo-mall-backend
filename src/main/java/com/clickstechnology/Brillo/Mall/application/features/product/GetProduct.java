package com.clickstechnology.Brillo.Mall.application.features.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetProduct {

    private final ProductService productService;

    public ProductDto execute(String productId) {
        return productService.findProductByProductId(productId);
    }

    public ProductDto executeBySku(String sku) {
        return productService.findProductBySku(sku);
    }
}
