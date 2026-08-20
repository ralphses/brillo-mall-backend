package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.CategoryCatalogService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PublicSearchService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.category.CategoryDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.search.PublicSearchResultDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.PublicSearchResultType;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import com.clickstechnology.Brillo.Mall.application.features.business.ListPublicServices;
import com.clickstechnology.Brillo.Mall.application.features.product.ListPublicProducts;
import com.clickstechnology.Brillo.Mall.application.features.publicread.PublicRead;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryCatalogService categoryCatalogService;

    @MockitoBean
    private PublicSearchService publicSearchService;

    @MockitoBean
    private ListPublicProducts listPublicProducts;

    @MockitoBean
    private ListPublicServices listPublicServices;

    @MockitoBean
    private PublicRead publicRead;

    @Test
    void getProductCategories_shouldReturnPublicCatalogWithoutJwt() throws Exception {
        when(categoryCatalogService.listProductCategories()).thenReturn(List.of(
                CategoryDto.builder().code("PHARMACY").label("Pharmacy").type(BusinessCategory.PRODUCTS).build(),
                CategoryDto.builder().code("GROCERY").label("Grocery").type(BusinessCategory.PRODUCTS).build()
        ));

        mockMvc.perform(get("/api/v1/public/product-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].code").value("PHARMACY"))
                .andExpect(jsonPath("$.data[0].label").value("Pharmacy"))
                .andExpect(jsonPath("$.data[1].code").value("GROCERY"));
    }

    @Test
    void search_shouldReturnMixedPublicFeedWithoutJwt() throws Exception {
        PaginatedResponse<PublicSearchResultDto> searchResponse = PaginatedResponse.<PublicSearchResultDto>builder()
                .page(1)
                .perPage(20)
                .total(2)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .items(List.of(
                        PublicSearchResultDto.builder()
                                .type(PublicSearchResultType.PRODUCT)
                                .id("prod-1")
                                .name("Herbal Soap")
                                .category("Pharmacy")
                                .build(),
                        PublicSearchResultDto.builder()
                                .type(PublicSearchResultType.BUSINESS)
                                .id("biz-1")
                                .name("Herbal Store")
                                .category("PRODUCTS")
                                .build()
                ))
                .build();

        when(publicSearchService.search("herbal", 1, 20)).thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1/public/search")
                        .param("q", "herbal")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items[0].type").value("PRODUCT"))
                .andExpect(jsonPath("$.data.items[0].name").value("Herbal Soap"))
                .andExpect(jsonPath("$.data.items[1].type").value("BUSINESS"))
                .andExpect(jsonPath("$.data.items[1].name").value("Herbal Store"));
    }

    @Test
    void getProduct_shouldReturnSinglePublicProductWithoutJwt() throws Exception {
        when(publicRead.getProduct("prod-1")).thenReturn(ProductDto.builder()
                .id("prod-1")
                .businessId("biz-1")
                .name("Flash Product")
                .flashSale(true)
                .build());

        mockMvc.perform(get("/api/v1/public/products/prod-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("prod-1"))
                .andExpect(jsonPath("$.data.flashSale").value(true));
    }

    @Test
    void getService_shouldReturnSinglePublicServiceWithoutJwt() throws Exception {
        when(publicRead.getService("service-1")).thenReturn(BusinessServiceDto.builder()
                .id("service-1")
                .businessId("biz-1")
                .name("Home Delivery")
                .slug("home-delivery")
                .build());

        mockMvc.perform(get("/api/v1/public/services/service-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("service-1"))
                .andExpect(jsonPath("$.data.slug").value("home-delivery"));
    }

    @Test
    void getBusiness_shouldReturnSinglePublicBusinessWithoutJwt() throws Exception {
        when(publicRead.getBusiness("biz-1")).thenReturn(BusinessDto.builder()
                .id("biz-1")
                .name("Herbal Store")
                .slug("herbal-store")
                .build());

        mockMvc.perform(get("/api/v1/public/businesses/biz-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("biz-1"))
                .andExpect(jsonPath("$.data.slug").value("herbal-store"));
    }

    @Test
    void getProducts_shouldReturnPublicCatalogWithoutJwt() throws Exception {
        PaginatedResponse<ProductDto> productResponse = PaginatedResponse.<ProductDto>builder()
                .page(1)
                .perPage(20)
                .total(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .items(List.of(ProductDto.builder()
                        .id("prod-1")
                        .name("Flash Product")
                        .flashSale(true)
                        .build()))
                .build();

        when(listPublicProducts.execute(
                eq("biz-1"),
                eq(1),
                eq(20),
                eq("shirt"),
                eq("PHARMACY"),
                eq(true),
                eq(java.math.BigDecimal.valueOf(10)),
                eq(java.math.BigDecimal.valueOf(100)),
                eq(true))).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/public/products")
                        .param("businessId", "biz-1")
                        .param("q", "shirt")
                        .param("category", "PHARMACY")
                        .param("flashSale", "true")
                        .param("minPrice", "10")
                        .param("maxPrice", "100")
                        .param("inStockOnly", "true")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value("prod-1"))
                .andExpect(jsonPath("$.data.items[0].flashSale").value(true));
    }

    @Test
    void getServices_shouldReturnPublicCatalogWithoutJwt() throws Exception {
        PaginatedResponse<BusinessServiceDto> serviceResponse = PaginatedResponse.<BusinessServiceDto>builder()
                .page(1)
                .perPage(20)
                .total(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .items(List.of(BusinessServiceDto.builder()
                        .id("service-1")
                        .name("Home Delivery")
                        .slug("home-delivery")
                        .businessId("biz-1")
                        .category("PHARMACY")
                        .pricingType(PricingType.FIXED)
                        .negotiable(true)
                        .requiresSchedule(false)
                        .active(true)
                        .build()))
                .build();

        when(listPublicServices.execute(
                eq("biz-1"),
                eq(1),
                eq(20),
                eq("delivery"),
                eq("PHARMACY"),
                eq(PricingType.FIXED),
                eq(true),
                eq(false))).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/v1/public/services")
                        .param("businessId", "biz-1")
                        .param("q", "delivery")
                        .param("category", "PHARMACY")
                        .param("pricingType", "FIXED")
                        .param("negotiable", "true")
                        .param("requiresSchedule", "false")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value("service-1"))
                .andExpect(jsonPath("$.data.items[0].slug").value("home-delivery"))
                .andExpect(jsonPath("$.data.items[0].pricingType").value("FIXED"));
    }
}
