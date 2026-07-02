package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.UpdateProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.features.product.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products")
public class ProductController {

    private final AddProduct addProduct;
    private final ListAllProducts listAllProducts;
    private final GetProduct getProduct;
    private final UpdateProduct updateProduct;
    private final DeleteProduct deleteProduct;

    @PostMapping("/{businessId}")
    public ResponseWrapper<ProductDto> addProduct(
            @PathVariable final String businessId,
            @RequestBody @Valid final AddProductRequest request,
            final HttpServletRequest httpServletRequest) {
        ProductDto response = addProduct.execute(businessId, request, httpServletRequest);
        return success(response);
    }

    @GetMapping
    public ResponseWrapper<PaginatedResponse<ProductDto>> getProducts(
            @RequestParam(required = false) final String businessId,
            @RequestParam(value = "page", defaultValue = "1") final Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") final Integer pageSize,
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final BigDecimal minPrice,
            @RequestParam(required = false) final BigDecimal maxPrice,
            @RequestParam(required = false) final Boolean inStockOnly,
            @RequestParam(required = false) final EntityStatus status) {
        PaginatedResponse<ProductDto> response = listAllProducts.execute(
                businessId,
                page,
                pageSize,
                search,
                minPrice,
                maxPrice,
                inStockOnly,
                status);
        return success(response);
    }

    @GetMapping("{productId}")
    public ResponseWrapper<ProductDto> getProduct(
            @PathVariable final String productId) {
        ProductDto response = getProduct.execute(productId);
        return success(response);
    }

    @GetMapping("by-sku/{sku}")
    public ResponseWrapper<ProductDto> getProductBySku(
            @PathVariable final String sku) {
        ProductDto response = getProduct.executeBySku(sku);
        return success(response);
    }

    @PutMapping("/{businessId}/{productId}")
    public ResponseWrapper<ProductDto> updateProduct(
            @PathVariable final String businessId,
            @PathVariable final String productId,
            @RequestBody @Valid final UpdateProductRequest request) {
        ProductDto response = updateProduct.execute(businessId, productId, request);
        return success(response);
    }

    @DeleteMapping("/{businessId}/{productId}")
    public ResponseWrapper<String> deleteProduct(
            @PathVariable final String businessId,
            @PathVariable final String productId) {
        deleteProduct.execute(businessId, productId);
        return success("Product deleted successfully");
    }
}
