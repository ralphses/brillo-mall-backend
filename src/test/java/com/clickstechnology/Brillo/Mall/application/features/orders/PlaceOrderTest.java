package com.clickstechnology.Brillo.Mall.application.features.orders;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemRequest;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderPlacedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceOrderTest {

    @Mock
    private OrderService orderService;

    @Mock
    private BusinessService businessService;

    @Mock
    private ProductService productService;

    @Mock
    private CustomerService customerService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AuthenticationUtil authenticationUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private PlaceOrder placeOrder;

    private PlaceOrderRequest placeOrderRequest;
    private CustomerDto customerDto;
    private ProductDto productDto;
    private OrderDto orderDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        customerDto = new CustomerDto();
        String customerId = UUID.randomUUID().toString();
        customerDto.setId(customerId);

        userDto = UserDto.builder()
                .id(UUID.randomUUID().toString())
                .username("testUser")
                .build();

        productDto = new ProductDto();
        productDto.setId("prod1");
        productDto.setBusinessId("biz1");
        productDto.setPrice(BigDecimal.TEN);

        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setProductId("prod1");
        orderItemRequest.setQuantity(1);

        placeOrderRequest = new PlaceOrderRequest();
        placeOrderRequest.setCustomer(customerDto);
        placeOrderRequest.setItems(List.of(orderItemRequest));

        orderDto = new OrderDto();
        orderDto.setId("order1");
    }

    @Test
    void execute_Success_NewOrder() {
        when(authenticationUtil.getAuthenticatedUsername(httpServletRequest)).thenReturn("testUser");
        when(userService.findByUsername(anyString())).thenReturn(userDto);
        when(customerService.resolveCustomer(any(CustomerDto.class), anyString(), Set.of("biz1"))).thenReturn(customerDto);
        when(productService.findProductsByIds(anyList())).thenReturn(List.of(productDto));
        when(productService.findAllByProductIds(anySet())).thenReturn(List.of(productDto));
        when(orderService.generateOrderId()).thenReturn("newOrderId");
        when(orderService.createNewOrder(anyString(), any(PlaceOrderRequest.class), any())).thenReturn(orderDto);

        OrderPlacedResponse response = placeOrder.execute(placeOrderRequest, httpServletRequest);

        assertNotNull(response);
        assertEquals("New order placed successfully", response.getMessage());
        assertEquals(orderDto, response.getOrder());

        verify(customerService).resolveCustomer(placeOrderRequest.getCustomer(), userDto.getId(), Set.of("biz1"));
        verify(productService).findProductsByIds(List.of("prod1"));
        verify(businessService).validateBusinessIsActive(Set.of("biz1"));
        verify(productService).checkInStock(Map.of("prod1", 1));
        verify(orderService).generateOrderId();
        verify(businessService).addCustomer(customerDto, Set.of("biz1"));
    }

    @Test
    void execute_Success_UpdateOrder() {
        placeOrderRequest.setOrderId("existingOrderId");

        when(authenticationUtil.getAuthenticatedUsername(httpServletRequest)).thenReturn("testUser");
        when(userService.findByUsername(anyString())).thenReturn(userDto);
        when(customerService.resolveCustomer(any(CustomerDto.class), anyString(), Set.of("biz1"))).thenReturn(customerDto);
        when(productService.findProductsByIds(anyList())).thenReturn(List.of(productDto));
        when(productService.findAllByProductIds(anySet())).thenReturn(List.of(productDto));
        when(orderService.findOrderById("existingOrderId")).thenReturn(orderDto);
        when(orderService.addItemsToOrder(anyString(), any(PlaceOrderRequest.class))).thenReturn(orderDto);

        OrderPlacedResponse response = placeOrder.execute(placeOrderRequest, httpServletRequest);

        assertNotNull(response);
        assertEquals("New order placed successfully", response.getMessage());
        assertEquals(orderDto, response.getOrder());

        verify(orderService).findOrderById("existingOrderId");
        verify(orderService).addItemsToOrder(orderDto.getId(), placeOrderRequest);
    }

    @Test
    void execute_Failure_NoProductsFound() {
        when(authenticationUtil.getAuthenticatedUsername(httpServletRequest)).thenReturn("testUser");
        when(userService.findByUsername(anyString())).thenReturn(userDto);
        when(customerService.resolveCustomer(any(CustomerDto.class), anyString(), Set.of("biz1"))).thenReturn(customerDto);
        when(productService.findProductsByIds(anyList())).thenReturn(Collections.emptyList());

        BusinessException exception = assertThrows(BusinessException.class, () -> placeOrder.execute(placeOrderRequest, httpServletRequest));

        assertEquals("No products found for the given IDs.", exception.getMessage());
    }

    @Test
    void execute_Failure_InactiveBusiness() {
        when(authenticationUtil.getAuthenticatedUsername(httpServletRequest)).thenReturn("testUser");
        when(userService.findByUsername(anyString())).thenReturn(userDto);
        when(customerService.resolveCustomer(any(CustomerDto.class), anyString(), Set.of("biz1"))).thenReturn(customerDto);
        when(productService.findProductsByIds(anyList())).thenReturn(List.of(productDto));
        when(productService.findAllByProductIds(anySet())).thenReturn(List.of(productDto));
        doThrow(new BusinessException("Business is inactive")).when(businessService).validateBusinessIsActive(anySet());

        BusinessException exception = assertThrows(BusinessException.class, () -> placeOrder.execute(placeOrderRequest, httpServletRequest));

        assertEquals("Business is inactive", exception.getMessage());
    }

    @Test
    void execute_Failure_OutOfStock() {
        when(authenticationUtil.getAuthenticatedUsername(httpServletRequest)).thenReturn("testUser");
        when(userService.findByUsername(anyString())).thenReturn(userDto);
        when(customerService.resolveCustomer(any(CustomerDto.class), anyString(), Set.of("biz1"))).thenReturn(customerDto);
        when(productService.findProductsByIds(anyList())).thenReturn(List.of(productDto));
        when(productService.findAllByProductIds(anySet())).thenReturn(List.of(productDto));
        doThrow(new BusinessException("Out of stock")).when(productService).checkInStock(anyMap());

        BusinessException exception = assertThrows(BusinessException.class, () -> placeOrder.execute(placeOrderRequest, httpServletRequest));

        assertEquals("Out of stock", exception.getMessage());
    }
}