package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemRequest;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "07035002025", authorities = {"ROLE_USER"})
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    private ProductDto product;
    private UserDto testUser;
    private CustomerDto testCustomer;

    @BeforeEach
    void setUp() {

        // Setup test user by registering them via the service
        String phoneNumber = "07035002025";
        String email = "testuser@test.com";
        RegisterRequest registerRequest = new RegisterRequest(
                "Test User",
                phoneNumber,
                "Password123",
                null
        );
        userService.registerNewUser(registerRequest, null, false);
        testUser = userService.findByUsername(phoneNumber);

        // 1. Onboard a new business using the service
        OnboardBusinessRequest businessRequest = new OnboardBusinessRequest();
        businessRequest.setBusinessName("Test Mart");
        businessService.createNew(businessRequest, testUser, "logo.png", BusinessCategory.PRODUCTS);
        BusinessDto business = businessService.findByBusinessSlug("test-mart");

        // 2. Add a product to the business using the service
        AddProductRequest productRequest = new AddProductRequest();
        productRequest.setName("Test Product");
        productRequest.setPrice(BigDecimal.valueOf(19.99));
        productRequest.setQuantity(10);
        product = productService.createProduct(business.getId(), productRequest, "logo.png");

        // --- GIVEN: A placed order ---
        CustomerDto customerDto = new CustomerDto();
        customerDto.setCustomerPhoneNumber(phoneNumber);
        customerDto.setAddress("123 Test Street");
        customerDto.setCustomerName(testUser.getFullName());
        customerDto.setCustomerEmail(testUser.getEmail());


        testCustomer = customerService.resolveCustomer(customerDto);
        System.out.println("testCustomer = " + testCustomer);
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should Place Order Successfully")
    void placeOrder_shouldSucceed_whenRequestIsValid() throws Exception {
        // Given
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(testUser.getId());
        customerDto.setCustomerPhoneNumber("07035002025");
        customerDto.setAddress("123 Test Street");
        customerDto.setCustomerName(testUser.getUsername());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(product.getId());
        itemRequest.setQuantity(5);
        itemRequest.setPrice(product.getPrice());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setCustomer(customerDto);
        request.setItems(List.of(itemRequest));
        request.setPaymentMethod(PaymentMethod.PAY_ON_DELIVERY);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.message").value("New order placed successfully"))
                .andExpect(jsonPath("$.data.order.id").isNotEmpty())
                .andExpect(jsonPath("$.data.order.totalAmount").value(99.95)); // 5 * 19.99

        // Verify stock is NOT reduced, as this now happens at checkout
        ProductDto updatedProduct = productService.findProductByProductId(product.getId());
        assertThat(updatedProduct.getQuantity()).isEqualTo(10);

        // The successful response and stock reduction are strong indicators of order creation.
        // We trust that if these are correct, the order was created.
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should Fail When Product Is Out Of Stock")
    void placeOrder_shouldFail_whenProductIsOutOfStock() throws Exception {
        // Given
        CustomerDto customerDto = new CustomerDto();
        customerDto.setCustomerPhoneNumber("07035002025");
        customerDto.setAddress("123 Test Street");
        customerDto.setId(testUser.getId());
        customerDto.setCustomerName(testUser.getUsername());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(product.getId());
        itemRequest.setQuantity(101); // More than available stock
        itemRequest.setPrice(product.getPrice());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setCustomer(customerDto);
        request.setItems(List.of(itemRequest));
        request.setPaymentMethod(PaymentMethod.PAY_ON_DELIVERY);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Insufficient stock for product : Test Product"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Should Fetch Order Successfully")
    void getOrder_shouldSucceed_whenOrderExists() throws Exception {

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(product.getId());
        itemRequest.setQuantity(1);
        itemRequest.setPrice(product.getPrice());

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        placeOrderRequest.setCustomer(testCustomer);
        placeOrderRequest.setItems(List.of(itemRequest));
        placeOrderRequest.setPaymentMethod(PaymentMethod.PAY_ON_DELIVERY);

        String responseString = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placeOrderRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract the order ID from the response
        String orderId = objectMapper.readTree(responseString).at("/data/order/id").asText();


        // --- WHEN & THEN: Fetch the order by its ID ---
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.totalAmount").value(19.99))
                .andExpect(jsonPath("$.data.items[0].product.name").value("Test Product"));
    }
}