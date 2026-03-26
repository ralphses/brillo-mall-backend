package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.UpdateProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;

public interface ProductService {
    void ensureProductNameDoesNotExist(String businessId, String name);
    void ensureProductSkuDoesNotExist(String businessId, String sku);
    ProductDto createProduct(String businessId, AddProductRequest request, String defaultProductLogoUrl);

    PaginatedResponse<ProductDto> getProducts(String businessId, Integer page, Integer pageSize);

    ProductDto findProductByProductId(String productId);

    ProductDto findProductBySku(String sku);

    void deleteProduct(String productId);

    ProductDto updateProduct(String productId, UpdateProductRequest request);
}