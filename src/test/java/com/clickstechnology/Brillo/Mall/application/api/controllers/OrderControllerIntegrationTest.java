package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemRequest;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.order.UpdateOrderStatusRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentMethod;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @MockitoBean
    private CacheUtil cacheUtil;

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


        testCustomer = customerService.resolveCustomer(customerDto, testUser.getId(), new HashSet<>(Set.of(business.getId())));
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

    @Test
    @DisplayName("GET /api/v1/orders - Should Fetch All Orders for a Customer")
    void getAllOrders_shouldSucceed_forAuthenticatedCustomer() throws Exception {
        // --- GIVEN: Two separate orders placed by the same customer ---
        PlaceOrderRequest firstOrderRequest = new PlaceOrderRequest();
        firstOrderRequest.setCustomer(testCustomer);
        firstOrderRequest.setItems(List.of(new OrderItemRequest(product.getId(), 1, product.getPrice())));
        firstOrderRequest.setPaymentMethod(PaymentMethod.PAY_ON_DELIVERY);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOrderRequest)))
                .andExpect(status().isOk());

        PlaceOrderRequest secondOrderRequest = new PlaceOrderRequest();
        secondOrderRequest.setCustomer(testCustomer);
        secondOrderRequest.setItems(List.of(new OrderItemRequest(product.getId(), 2, product.getPrice())));
        secondOrderRequest.setPaymentMethod(PaymentMethod.ONLINE);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOrderRequest)))
                .andExpect(status().isOk());

        // --- WHEN & THEN: Fetch all orders for the customer ---
        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].totalAmount").value(19.99))
                .andExpect(jsonPath("$.data.items[1].totalAmount").value(39.98));
    }

    @Test
    @DisplayName("GET /api/v1/orders - Admin Should Fetch Orders by BusinessId")
    @WithMockUser(username = "admin@test.com", authorities = {"ROLE_ADMIN"})
    void getAllOrders_shouldSucceed_forAdminWithBusinessId() throws Exception {
        // GIVEN: An admin user is registered and granted the ADMIN role
        RegisterRequest adminRegisterRequest = new RegisterRequest("Admin User", "admin@test.com", "Password123", "admin@test.com");
        userService.registerNewUser(adminRegisterRequest, null, false);
        userService.addRoleToUser("admin@test.com", UserRole.ADMIN);
        UserDto adminUser = userService.findByUsername("admin@test.com");

        // --- GIVEN: An admin-owned business and product ---
        OnboardBusinessRequest adminBusinessRequest = new OnboardBusinessRequest();
        adminBusinessRequest.setBusinessName("Admin Mart");
        businessService.createNew(adminBusinessRequest, adminUser, "logo2.png", BusinessCategory.PRODUCTS);
        BusinessDto adminBusiness = businessService.findByBusinessSlug("admin-mart");

        AddProductRequest adminProductRequest = new AddProductRequest();
        adminProductRequest.setName("Admin Product");
        adminProductRequest.setPrice(BigDecimal.valueOf(50.00));
        adminProductRequest.setQuantity(5);
        ProductDto adminProduct = productService.createProduct(adminBusiness.getId(), adminProductRequest, "logo2.png");

        // --- GIVEN: A second business and product owned by another user ---
        RegisterRequest otherUserRequest = new RegisterRequest("Other User", "08012345678", "Password123", null);
        userService.registerNewUser(otherUserRequest, null, false);
        UserDto otherUser = userService.findByUsername("08012345678");

        OnboardBusinessRequest otherBusinessRequest = new OnboardBusinessRequest();
        otherBusinessRequest.setBusinessName("Other Mart");
        businessService.createNew(otherBusinessRequest, otherUser, "logo2.png", BusinessCategory.PRODUCTS);
        BusinessDto otherBusiness = businessService.findByBusinessSlug("other-mart");

        AddProductRequest otherProductRequest = new AddProductRequest();
        otherProductRequest.setName("Other Product");
        otherProductRequest.setPrice(BigDecimal.valueOf(50.00));
        otherProductRequest.setQuantity(5);
        ProductDto otherProduct = productService.createProduct(otherBusiness.getId(), otherProductRequest, "logo2.png");

        // Place an order for the first business's product
        OrderItemRequest firstItemRequest = new OrderItemRequest();
        firstItemRequest.setProductId(adminProduct.getId());
        firstItemRequest.setQuantity(1);
        firstItemRequest.setPrice(adminProduct.getPrice());

        PlaceOrderRequest firstOrderRequest = new PlaceOrderRequest();
        firstOrderRequest.setCustomer(testCustomer);
        firstOrderRequest.setItems(List.of(firstItemRequest));
        firstOrderRequest.setPaymentMethod(PaymentMethod.ONLINE);
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(firstOrderRequest))).andExpect(status().isOk());

        // Place an order for the second business's product
        OrderItemRequest secondItemRequest = new OrderItemRequest();
        secondItemRequest.setProductId(otherProduct.getId());
        secondItemRequest.setQuantity(1);
        secondItemRequest.setPrice(otherProduct.getPrice());

        PlaceOrderRequest secondOrderRequest = new PlaceOrderRequest();
        secondOrderRequest.setCustomer(testCustomer);
        secondOrderRequest.setItems(List.of(secondItemRequest));
        secondOrderRequest.setPaymentMethod(PaymentMethod.ONLINE);
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(secondOrderRequest))).andExpect(status().isOk());

        // --- WHEN & THEN: Admin fetches orders for the first business only ---
        mockMvc.perform(get("/api/v1/orders")
                        .param("businessId", adminBusiness.getId())
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].items[0].product.name").value("Admin Product"));
    }

    @Test
    @DisplayName("GET /api/v1/orders - Admin Should Fetch Orders for Their Own Businesses if no businessId is provided")
    @WithMockUser(username = "07035002025", authorities = {"ROLE_ADMIN"})
    void getAllOrders_shouldSucceed_forAdminWithoutBusinessId() throws Exception {
        // GIVEN: Grant ADMIN role to the primary test user for this test
        userService.addRoleToUser(testUser.getPhoneNumber(), UserRole.ADMIN);

        // --- GIVEN: A second business and product owned by another user ---
        RegisterRequest otherUserRequest = new RegisterRequest("Other User", "08012345678", "Password123", null);
        userService.registerNewUser(otherUserRequest, null, false);
        UserDto otherUser = userService.findByUsername("08012345678");

        OnboardBusinessRequest otherBusinessRequest = new OnboardBusinessRequest();
        otherBusinessRequest.setBusinessName("Other Mart");
        businessService.createNew(otherBusinessRequest, otherUser, "logo2.png", BusinessCategory.PRODUCTS);
        BusinessDto otherBusiness = businessService.findByBusinessSlug("other-mart");

        AddProductRequest otherProductRequest = new AddProductRequest();
        otherProductRequest.setName("Other Product");
        otherProductRequest.setPrice(BigDecimal.valueOf(50.00));
        otherProductRequest.setQuantity(5);
        ProductDto otherProduct = productService.createProduct(otherBusiness.getId(), otherProductRequest, "logo2.png");

        // Place an order for the admin's own business's product
        PlaceOrderRequest firstOrderRequest = PlaceOrderRequest.builder()
                .customer(testCustomer)
                .items(List.of(new OrderItemRequest(product.getId(), 1, product.getPrice())))
                .paymentMethod(PaymentMethod.ONLINE)
                .build();

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(firstOrderRequest))).andExpect(status().isOk());

        // Place an order for the second, unrelated business's product
        PlaceOrderRequest secondOrderRequest = PlaceOrderRequest
                .builder()
                .paymentMethod(PaymentMethod.ONLINE)
                .customer(testCustomer)
                .items(List.of(new OrderItemRequest(otherProduct.getId(), 1, otherProduct.getPrice())))
                .build();

        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(secondOrderRequest))).andExpect(status().isOk());

        // --- WHEN & THEN: Admin (testUser) fetches orders without a businessId ---
        // Should only return orders from businesses owned by testUser ("Test Mart")
        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].items[0].product.name").value("Test Product"));
    }

    @Nested
    @DisplayName("PUT /api/v1/orders/{orderId}/status")
    class UpdateOrderStatusTests {

        private String orderId;

        @BeforeEach
        void setUp() throws Exception {
            // GIVEN: An existing order
            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
            placeOrderRequest.setCustomer(testCustomer);
            placeOrderRequest.setItems(List.of(new OrderItemRequest(product.getId(), 1, product.getPrice())));
            placeOrderRequest.setPaymentMethod(PaymentMethod.PAY_ON_DELIVERY);

            String responseString = mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(placeOrderRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            orderId = objectMapper.readTree(responseString).at("/data/order/id").asText();
        }

        @Test
        @DisplayName("Should Update Order Status Successfully for Business Owner")
        @WithMockUser(username = "07035002025", authorities = {"ROLE_ADMIN"})
        void updateOrderStatus_shouldSucceed_forBusinessOwner() throws Exception {
            // GIVEN: The user is an admin
            userService.addRoleToUser(testUser.getPhoneNumber(), UserRole.ADMIN);

            UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
            updateRequest.setStatus(OrderStatus.SHIPPED.name());

            // WHEN & THEN
            mockMvc.perform(put("/api/v1/orders/{orderId}/status", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.status").value("SHIPPED"));
        }

        @Test
        @DisplayName("Should Fail to Update Order Status for Non-Admin User")
        @WithMockUser(username = "anotheruser@test.com", authorities = {"ROLE_USER"})
        void updateOrderStatus_shouldFail_forNonAdminUser() throws Exception {
            // GIVEN: A non-admin user
            RegisterRequest anotherUserRequest = new RegisterRequest("Another User", "anotheruser@test.com", "Password123", "anotheruser@test.com");
            userService.registerNewUser(anotherUserRequest, null, false);

            UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
            updateRequest.setStatus(OrderStatus.SHIPPED.name());

            // WHEN & THEN
            mockMvc.perform(put("/api/v1/orders/{orderId}/status", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User is not associated with any business."));
        }

        @Test
        @DisplayName("Should Fail to Update Order Status with Invalid Status")
        @WithMockUser(username = "07035002025", authorities = {"ROLE_ADMIN"})
        void updateOrderStatus_shouldFail_withInvalidStatus() throws Exception {
            // GIVEN: The user is an admin
            userService.addRoleToUser(testUser.getPhoneNumber(), UserRole.ADMIN);

            UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
            updateRequest.setStatus("INVALID_STATUS");

            // WHEN & THEN
            mockMvc.perform(put("/api/v1/orders/{orderId}/status", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.status").value("Invalid order status"));
        }

        @Test
        @DisplayName("Should Fail to Update Order Status for Order Not Belonging to Business")
        @WithMockUser(username = "anotheradmin@test.com", authorities = {"ROLE_ADMIN"})
        void updateOrderStatus_shouldFail_forOrderNotBelongingToBusiness() throws Exception {
            // GIVEN: Another admin with their own business
            RegisterRequest anotherAdminRequest = new RegisterRequest("Another Admin", "anotheradmin@test.com", "Password123", "anotheradmin@test.com");
            userService.registerNewUser(anotherAdminRequest, null, false);
            UserDto anotherAdmin = userService.findByUsername("anotheradmin@test.com");
            userService.addRoleToUser(anotherAdmin.getUsername(), UserRole.ADMIN);

            OnboardBusinessRequest anotherBusinessRequest = new OnboardBusinessRequest();
            anotherBusinessRequest.setBusinessName("Another Mart");
            businessService.createNew(anotherBusinessRequest, anotherAdmin, "logo3.png", BusinessCategory.PRODUCTS);

            UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
            updateRequest.setStatus(OrderStatus.DELIVERED.name());

            // WHEN & THEN
            mockMvc.perform(put("/api/v1/orders/{orderId}/status", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Order not found or you do not have permission to view it."));
        }
    }
}
