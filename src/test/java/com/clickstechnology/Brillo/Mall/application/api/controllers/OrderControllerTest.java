package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemRequest;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderPlacedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentMethod;
import com.clickstechnology.Brillo.Mall.application.features.orders.PlaceOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceOrder placeOrder;

    private PlaceOrderRequest validRequest;
    private OrderPlacedResponse orderPlacedResponse;

    @BeforeEach
    void setUp() {
        validRequest = new PlaceOrderRequest();
        // Assume OrderItemRequest and CustomerDto are set up correctly
        // For simplicity, we'll just ensure the list isn't empty
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId("prod1");
        item.setQuantity(1);
        item.setPrice(BigDecimal.TEN);
        validRequest.setItems(Collections.singletonList(item));
        validRequest.setPaymentMethod(PaymentMethod.ONLINE);

        CustomerDto customer = new CustomerDto();
        customer.setId("customer1");

        validRequest.setCustomer(customer);


        OrderDto orderDto = new OrderDto();
        orderDto.setId("order123");
        orderPlacedResponse = new OrderPlacedResponse("Order placed successfully", orderDto);
    }

    @Test
    @DisplayName("POST /api/v1/orders - Success")
    void placeOrder_shouldReturnSuccessResponse_whenRequestIsValid() throws Exception {
        // Given
        when(placeOrder.execute(any(PlaceOrderRequest.class), any())).thenReturn(orderPlacedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.data.order.id").value("order123"));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Validation Failure")
    void placeOrder_shouldReturnBadRequest_whenItemsAreEmpty() throws Exception {
        // Given
        PlaceOrderRequest invalidRequest = new PlaceOrderRequest();
        invalidRequest.setItems(Collections.emptyList()); // Invalid because @NotEmpty

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}