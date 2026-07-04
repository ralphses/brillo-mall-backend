package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.features.product.AddProduct;
import com.clickstechnology.Brillo.Mall.application.features.product.ListAllProducts;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddProduct addProduct;

    @MockitoBean
    private ListAllProducts listAllProducts;

    private String businessId;
    private AddProductRequest validRequest;
    private ProductDto expectedResponse;

    @BeforeEach
    void setUp() {
        businessId = "biz-123";
        validRequest = new AddProductRequest(
                "Test Product",
                "PHARMACY",
                "Test Description",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(90.00),
                "SKU-123",
                true,
                50
        );

        expectedResponse = ProductDto.builder()
                .id("prod-123")
                .businessId(businessId)
                .name("Test Product")
                .category("PHARMACY")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.00))
                .discountedPrice(BigDecimal.valueOf(90.00))
                .sku("SKU-123")
                .flashSale(true)
                .quantity(50)
                .status(EntityStatus.ACTIVE)
                .build();
    }

    @Test
    void addProduct_Success() throws Exception {
        when(addProduct.execute(eq(businessId), any(AddProductRequest.class), any(HttpServletRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/products/{businessId}", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("prod-123"))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.category").value("PHARMACY"))
                .andExpect(jsonPath("$.data.flashSale").value(true))
                .andExpect(jsonPath("$.data.sku").value("SKU-123"));
    }

    @Test
    void addProduct_ValidationFailure_BlankName() throws Exception {
        validRequest.setName("");

        mockMvc.perform(post("/api/v1/products/{businessId}", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProduct_ValidationFailure_NullPrice() throws Exception {
        validRequest.setPrice(null);

        mockMvc.perform(post("/api/v1/products/{businessId}", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProduct_ValidationFailure_NegativePrice() throws Exception {
        validRequest.setPrice(BigDecimal.valueOf(-10.00));

        mockMvc.perform(post("/api/v1/products/{businessId}", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProduct_ValidationFailure_NullQuantity() throws Exception {
        validRequest.setQuantity(null);

        mockMvc.perform(post("/api/v1/products/{businessId}", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProduct_BusinessException() throws Exception {
        when(addProduct.execute(eq(businessId), any(AddProductRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new BusinessException("Business is not active"));

        mockMvc.perform(post("/api/v1/products/{businessId}", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Business is not active"));
    }

    @Test
    void getProducts_WithFilters_Success() throws Exception {
        PaginatedResponse<ProductDto> paginatedResponse = PaginatedResponse.<ProductDto>builder()
                .items(List.of(expectedResponse))
                .page(1)
                .perPage(20)
                .total(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(listAllProducts.execute(
                eq(businessId),
                eq(1),
                eq(20),
                eq("shirt"),
                eq(BigDecimal.valueOf(10)),
                eq(BigDecimal.valueOf(100)),
                eq(true),
                eq(EntityStatus.ACTIVE)))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/v1/products")
                        .param("businessId", businessId)
                        .param("page", "1")
                        .param("pageSize", "20")
                        .param("search", "shirt")
                        .param("minPrice", "10")
                        .param("maxPrice", "100")
                        .param("inStockOnly", "true")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.items[0].id").value("prod-123"))
                .andExpect(jsonPath("$.data.items[0].name").value("Test Product"));
    }
}
