package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.CategoryCatalogService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PublicSearchService;
import com.clickstechnology.Brillo.Mall.application.dto.category.CategoryDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.dto.search.PublicSearchResultDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.features.business.ListPublicServices;
import com.clickstechnology.Brillo.Mall.application.features.product.ListPublicProducts;
import com.clickstechnology.Brillo.Mall.application.features.publicread.PublicRead;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/public")
@Tag(name = "Public", description = "Public storefront, catalog, and search APIs")
public class PublicController {

    private final CategoryCatalogService categoryCatalogService;
    private final PublicSearchService publicSearchService;
    private final ListPublicProducts listPublicProducts;
    private final ListPublicServices listPublicServices;
    private final PublicRead publicRead;

    @GetMapping("product-categories")
    @Operation(summary = "List product categories")
    public ResponseWrapper<List<CategoryDto>> getProductCategories() {
        return success(categoryCatalogService.listProductCategories());
    }

    @GetMapping("search")
    @Operation(summary = "Search public marketplace")
    public ResponseWrapper<PaginatedResponse<PublicSearchResultDto>> search(
            @RequestParam("q") final String q,
            @RequestParam(value = "page", defaultValue = "1") final Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") final Integer pageSize) {
        return success(publicSearchService.search(q, page, pageSize));
    }

    @GetMapping("products/{productId}")
    @Operation(summary = "Get public product")
    public ResponseWrapper<ProductDto> getProduct(
            @PathVariable final String productId) {
        return success(publicRead.getProduct(productId));
    }

    @GetMapping("products")
    @Operation(summary = "List public products")
    public ResponseWrapper<PaginatedResponse<ProductDto>> getProducts(
            @RequestParam(required = false) final String businessId,
            @RequestParam(required = false) final String q,
            @RequestParam(required = false) final String category,
            @RequestParam(required = false) final Boolean flashSale,
            @RequestParam(required = false) final BigDecimal minPrice,
            @RequestParam(required = false) final BigDecimal maxPrice,
            @RequestParam(required = false) final Boolean inStockOnly,
            @RequestParam(value = "page", defaultValue = "1") final Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") final Integer pageSize) {
        return success(listPublicProducts.execute(
                businessId,
                page,
                pageSize,
                q,
                category,
                flashSale,
                minPrice,
                maxPrice,
                inStockOnly));
    }

    @GetMapping("services")
    @Operation(summary = "List public services")
    public ResponseWrapper<PaginatedResponse<BusinessServiceDto>> getServices(
            @RequestParam(required = false) final String businessId,
            @RequestParam(required = false) final String q,
            @RequestParam(required = false) final String category,
            @RequestParam(required = false) final PricingType pricingType,
            @RequestParam(required = false) final Boolean negotiable,
            @RequestParam(required = false) final Boolean requiresSchedule,
            @RequestParam(value = "page", defaultValue = "1") final Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") final Integer pageSize) {
        return success(listPublicServices.execute(
                businessId,
                page,
                pageSize,
                q,
                category,
                pricingType,
                negotiable,
                requiresSchedule));
    }

    @GetMapping("services/{serviceId}")
    @Operation(summary = "Get public service")
    public ResponseWrapper<BusinessServiceDto> getService(
            @PathVariable final String serviceId) {
        return success(publicRead.getService(serviceId));
    }

    @GetMapping("businesses/{businessId}")
    @Operation(summary = "Get public business by id")
    public ResponseWrapper<BusinessDto> getBusiness(
            @PathVariable final String businessId) {
        return success(publicRead.getBusiness(businessId));
    }

    @GetMapping("businesses/slug/{businessSlug}")
    @Operation(summary = "Get public business by slug")
    public ResponseWrapper<BusinessDto> getBusinessBySlug(
            @PathVariable final String businessSlug) {
        return success(publicRead.getBusinessBySlug(businessSlug));
    }
}
