package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;

public interface ProductService {
    void ensureProductNameDoesNotExist(String businessId, String name);
    void ensureProductSkuDoesNotExist(String businessId, String sku);
    ProductDto createProduct(String businessId, AddProductRequest request);
}