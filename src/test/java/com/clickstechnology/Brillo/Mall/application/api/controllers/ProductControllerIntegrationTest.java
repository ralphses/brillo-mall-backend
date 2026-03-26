package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.application.features.product.AddProduct;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private String businessId;
    private AddProductRequest validRequest;
    private ProductDto expectedResponse;

    @BeforeEach
    void setUp() {
        businessId = "biz-123";
        validRequest = new AddProductRequest(
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(90.00),
                "SKU-123",
                50
        );

        expectedResponse = ProductDto.builder()
                .id("prod-123")
                .businessId(businessId)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.00))
                .discountedPrice(BigDecimal.valueOf(90.00))
                .sku("SKU-123")
                .quantity(50)
                .status(EntityStatus.ACTIVE)
                .build();
    }

    @Test
    void addProduct_Success() throws Exception {
        when(addProduct.execute(eq(businessId), any(AddProductRequest.class), any(HttpServletRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/products", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("prod-123"))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.sku").value("SKU-123"));
    }

    @Test
    void addProduct_ValidationFailure_BlankName() throws Exception {
        validRequest.setName("");

        mockMvc.perform(post("/api/v1/businesses/{businessId}/products", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProduct_ValidationFailure_NullPrice() throws Exception {
        validRequest.setPrice(null);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/products", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProduct_ValidationFailure_NegativePrice() throws Exception {
        validRequest.setPrice(BigDecimal.valueOf(-10.00));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/products", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProduct_ValidationFailure_NullQuantity() throws Exception {
        validRequest.setQuantity(null);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/products", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProduct_BusinessException() throws Exception {
        when(addProduct.execute(eq(businessId), any(AddProductRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new BusinessException("Business is not active"));

        mockMvc.perform(post("/api/v1/businesses/{businessId}/products", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Business is not active"));
    }
}