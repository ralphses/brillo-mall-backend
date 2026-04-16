package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.UpdateProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductService {
    void ensureProductNameDoesNotExist(String businessId, String name);
    void ensureProductSkuDoesNotExist(String businessId, String sku);
    ProductDto createProduct(String businessId, AddProductRequest request, String defaultProductLogoUrl);

    PaginatedResponse<ProductDto> getProducts(String businessId, Integer page, Integer pageSize);

    ProductDto findProductByProductId(String productId);

    ProductDto findProductBySku(String sku);

    void deleteProduct(String productId);

    ProductDto updateProduct(String productId, UpdateProductRequest request);

    List<ProductDto> findProductsByIds(List<String> productIds);

    List<ProductDto> findAllByProductIds(Set<String> productIds);

    void checkInStock(Map<String, Integer> mappedProductQuantityMap);

    void reduceStock(Map<String, Integer> mappedProductQuantityMap);
}