package com.clickstechnology.Brillo.Mall.application.features.orders;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.enums.RequestSource;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageOrderTest {

    @Mock
    private OrderService orderService;
    @Mock
    private CustomerService customerService;
    @Mock
    private BusinessService businessService;
    @Mock
    private ProductService productService;
    @Mock
    private TenantContextResolver tenantContextResolver;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ManageOrder manageOrder;

    private UserDto adminUser;
    private UserDto customerUser;
    private CustomerDto customerDto;
    private OrderDto orderDto;
    private BusinessDto businessDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        adminUser = new UserDto();
        adminUser.setId("admin-user-id");
        adminUser.setUsername("admin@example.com");
        adminUser.setRoles(List.of(UserRole.ADMIN.name()));

        customerUser = new UserDto();
        customerUser.setId("customer-user-id");
        customerUser.setUsername("customer@example.com");
        customerUser.setRoles(List.of(UserRole.CUSTOMER.name()));

        customerDto = new CustomerDto();
        customerDto.setId("customer-id");

        businessDto = new BusinessDto();
        businessDto.setId("business-id");
        businessDto.setName("Test Business");
        businessDto.setOwnerId(adminUser.getId());

        productDto = ProductDto.builder()
                .id("product-id")
                .businessId("business-id")
                .name("Test Product")
                .build();

        OrderItemDto orderItemDto = OrderItemDto.builder()
                .product(ProductDto.builder()
                        .id("product-id")
                        .businessId("business-id")
                        .build())
                .build();

        orderDto = new OrderDto();
        orderDto.setId("order-id");
        orderDto.setBusinessId("business-id");
        orderDto.setItems(List.of(orderItemDto));
    }

    @Test
    void findOrderById_ForWhatsAppRequest_ShouldReturnEnrichedOrder() {
        // Given
        String orderId = "order-id";
        String ownerId = "+1234567890";

        when(orderService.findOrderById(orderId)).thenReturn(orderDto);
        when(businessService.findAllByBusinessIds(Set.of("business-id"))).thenReturn(List.of(businessDto));

        // When
        OrderDto result = manageOrder.findOrderById(orderId, RequestSource.WHATSAPP, httpServletRequest, ownerId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertNotNull(result.getItems().getFirst().getBusiness());
        assertEquals("Test Business", result.getItems().getFirst().getBusiness().getName());

        verify(orderService).findOrderById(orderId);
        verify(businessService).findAllByBusinessIds(Set.of("business-id"));
    }

    @Test
    void findOrderById_ForWebAdminRequest_ShouldReturnEnrichedOrder() {
        // Given
        String orderId = "order-id";
        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(adminUser);
        when(tenantContextResolver.ownedBusinessIds(httpServletRequest)).thenReturn(List.of(businessDto.getId()));
        when(orderService.findOrderForBusinessAdmin(orderId, List.of(businessDto.getId()))).thenReturn(orderDto);
        when(productService.findProductsByIds(List.of(productDto.getId()))).thenReturn(List.of(productDto));

        // When
        OrderDto result = manageOrder.findOrderById(orderId, RequestSource.WEB, httpServletRequest, null);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertNotNull(result.getItems().getFirst().getProduct());
        assertEquals("Test Product", result.getItems().getFirst().getProduct().getName());

        verify(tenantContextResolver).ownedBusinessIds(httpServletRequest);
        verify(orderService).findOrderForBusinessAdmin(orderId, List.of(businessDto.getId()));
        verify(productService).findProductsByIds(List.of(productDto.getId()));
    }

    @Test
    void findOrderById_ForWebAdminWithNoBusinesses_ShouldThrowException() {
        // Given
        String orderId = "order-id";
        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(adminUser);
        when(tenantContextResolver.ownedBusinessIds(httpServletRequest)).thenReturn(Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> manageOrder.findOrderById(orderId, RequestSource.WEB, httpServletRequest, null));

        assertEquals("Admin user does not own any businesses.", exception.getMessage());
        verify(tenantContextResolver).ownedBusinessIds(httpServletRequest);
    }

    @Test
    void findOrderById_ForWebCustomerRequest_ShouldReturnEnrichedOrder() {
        // Given
        String orderId = "order-id";
        customerUser.setPhoneNumber("1234567890");

        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(customerUser);
        when(orderService.findOrderById(orderId)).thenReturn(orderDto);
        when(businessService.findAllByBusinessIds(Set.of("business-id"))).thenReturn(List.of(businessDto));

        // When
        OrderDto result = manageOrder.findOrderById(orderId, RequestSource.WEB, httpServletRequest, null);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertNotNull(result.getItems().getFirst().getBusiness());
        assertEquals("Test Business", result.getItems().getFirst().getBusiness().getName());

        verify(orderService).findOrderById(orderId);
        verify(businessService).findAllByBusinessIds(Set.of("business-id"));
        verify(customerService, never()).findByPhoneOrEmail(any());
    }

    @Test
    void findOrderById_ForWebCustomer_ShouldNotUseCustomerLookup() {
        // Given
        String orderId = "order-id";
        customerUser.setPhoneNumber("1234567890");
        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(customerUser);
        when(orderService.findOrderById(orderId)).thenReturn(orderDto);
        when(businessService.findAllByBusinessIds(Set.of("business-id"))).thenReturn(List.of(businessDto));

        // When
        OrderDto result = manageOrder.findOrderById(orderId, RequestSource.WEB, httpServletRequest, null);

        // Then
        assertNotNull(result);
        verify(customerService, never()).findByPhoneOrEmail(any());
        verify(orderService).findOrderById(orderId);
    }

    @Test
    void findOrderById_ForWebCustomer_ThrowsException_WhenOrderNotFound() {
        // Given
        String orderId = "order-id";
        customerUser.setPhoneNumber("1234567890");

        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(customerUser);
        when(orderService.findOrderById(orderId)).thenThrow(new BusinessException("Order not found"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> manageOrder.findOrderById(orderId, RequestSource.WEB, httpServletRequest, null));

        assertEquals("Order not found", exception.getMessage());
        verify(orderService).findOrderById(orderId);
    }

    @Test
    void findOrderById_ForWebAdmin_ThrowsException_WhenOrderNotFound() {
        // Given
        String orderId = "order-id";
        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(adminUser);
        when(tenantContextResolver.ownedBusinessIds(httpServletRequest)).thenReturn(List.of(businessDto.getId()));
        when(orderService.findOrderForBusinessAdmin(orderId, List.of(businessDto.getId()))).thenThrow(new BusinessException("Order not found"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> manageOrder.findOrderById(orderId, RequestSource.WEB, httpServletRequest, null));

        assertEquals("Order not found", exception.getMessage());
        verify(productService, never()).findProductsByIds(any());
    }
}
