package com.clickstechnology.Brillo.Mall.domain.orders;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemRequest;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentMethod;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CacheUtil cacheUtil;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    private Order order;
    private PlaceOrderRequest placeOrderRequest;
    private CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        customerDto = new CustomerDto();
        customerDto.setId(UUID.randomUUID().toString());
        customerDto.setAddress("123 Main St");

        order = new Order();
        order.setOrderId("order1");
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setCustomerId(customerDto.getId());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId("prod1");
        itemRequest.setQuantity(2);
        itemRequest.setPrice(BigDecimal.valueOf(50));

        placeOrderRequest = new PlaceOrderRequest();
        placeOrderRequest.setCustomer(customerDto);
        placeOrderRequest.setItems(List.of(itemRequest));
        placeOrderRequest.setPaymentMethod(PaymentMethod.ONLINE);
    }

    @Nested
    @DisplayName("findOrderById tests")
    class FindOrderByIdTests {

        @Test
        @DisplayName("Should return order from cache when it exists")
        void findOrderById_shouldReturnOrderFromCache_whenExists() {
            // Given
            String orderId = "order1";
            String cacheKey = CacheNames.ORDER_ID + orderId;
            when(cacheUtil.get(cacheKey, Order.class)).thenReturn(order);

            // When
            OrderDto result = orderServiceImpl.findOrderById(orderId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
            verify(orderRepository, never()).findByOrderId(anyString());
        }

        @Test
        @DisplayName("Should return order from repository when not in cache")
        void findOrderById_shouldReturnOrderFromRepository_whenNotInCache() {
            // Given
            String orderId = "order1";
            String cacheKey = CacheNames.ORDER_ID + orderId;
            when(cacheUtil.get(cacheKey, Order.class)).thenReturn(null);
            when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

            // When
            OrderDto result = orderServiceImpl.findOrderById(orderId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
            verify(orderRepository).findByOrderId(orderId);
        }

        @Test
        @DisplayName("Should throw BusinessException when order does not exist")
        void findOrderById_shouldThrowBusinessException_whenOrderDoesNotExist() {
            // Given
            String orderId = "nonexistent";
            String cacheKey = CacheNames.ORDER_ID + orderId;
            when(cacheUtil.get(cacheKey, Order.class)).thenReturn(null);
            when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderServiceImpl.findOrderById(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Order with ID: " + orderId + " does not exist");
        }
    }

    @Nested
    @DisplayName("addItemsToOrder tests")
    class AddItemsToOrderTests {

        @Test
        @DisplayName("Should add items to an existing order")
        void addItemsToOrder_shouldAddItemsToExistingOrder() {
            // Given
            String orderId = "order1";
            when(cacheUtil.get(anyString(), eq(Order.class))).thenReturn(order);
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // When
            OrderDto result = orderServiceImpl.addItemsToOrder(orderId, placeOrderRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200)); // 100 (initial) + 100 (new)
            assertThat(order.getItems()).hasSize(1);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Should throw BusinessException when adding items to nonexistent order")
        void addItemsToOrder_shouldThrowBusinessException_whenOrderDoesNotExist() {
            // Given
            String orderId = "nonexistent";
            when(cacheUtil.get(anyString(), eq(Order.class))).thenReturn(null);
            when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderServiceImpl.addItemsToOrder(orderId, placeOrderRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Order with ID: " + orderId + " does not exist");
        }
    }

    @Nested
    @DisplayName("createNewOrder tests")
    class CreateNewOrderTests {

        @Test
        @DisplayName("Should create a new order successfully")
        void createNewOrder_shouldCreateNewOrderSuccessfully() {
            // Given
            String newOrderId = "newOrder1";
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order savedOrder = invocation.getArgument(0);
                savedOrder.setId(1L); // Simulate saving and getting an ID
                return savedOrder;
            });

            // When
            OrderDto result = orderServiceImpl.createNewOrder(newOrderId, placeOrderRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(newOrderId);
            assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.valueOf(100));
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("findOrderByIdAndCustomerId tests")
    class FindOrderByIdAndCustomerIdTests {

        @Test
        @DisplayName("Should return order when found for customer")
        void findOrderByIdAndCustomerId_shouldReturnOrder_whenFound() {
            // Given
            String orderId = "order1";
            String customerId = customerDto.getId();
            when(orderRepository.findOrderDetailsByOrderIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));

            // When
            OrderDto result = orderServiceImpl.findOrderByIdAndCustomerId(orderId, customerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should throw BusinessException when order not found for customer")
        void findOrderByIdAndCustomerId_shouldThrowException_whenNotFound() {
            // Given
            String orderId = "order1";
            String customerId = "wrongCustomer";
            when(orderRepository.findOrderDetailsByOrderIdAndCustomerId(orderId, customerId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderServiceImpl.findOrderByIdAndCustomerId(orderId, customerId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Order not found or does not belong to the customer");
        }
    }

    @Nested
    @DisplayName("findOrderDetailsForCustomer tests")
    class FindOrderDetailsForCustomerTests {

        @Test
        @DisplayName("Should return order details when found for customer")
        void findOrderDetailsForCustomer_shouldReturnOrder_whenFound() {
            // Given
            String orderId = "order1";
            String customerId = customerDto.getId();
            when(orderRepository.findOrderDetailsByOrderIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));

            // When
            OrderDto result = orderServiceImpl.findOrderDetailsForCustomer(orderId, customerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should throw BusinessException when order details not found for customer")
        void findOrderDetailsForCustomer_shouldThrowException_whenNotFound() {
            // Given
            String orderId = "order1";
            String customerId = "wrongCustomer";
            when(orderRepository.findOrderDetailsByOrderIdAndCustomerId(orderId, customerId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderServiceImpl.findOrderDetailsForCustomer(orderId, customerId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Order not found or does not belong to the customer.");
        }
    }

    @Nested
    @DisplayName("findOrderForBusinessAdmin tests")
    class FindOrderForBusinessAdminTests {

        @Test
        @DisplayName("Should return order when admin has permission")
        void findOrderForBusinessAdmin_shouldReturnOrder_whenAdminHasPermission() {
            // Given
            String orderId = "order1";
            List<String> businessIds = List.of("biz1");
            when(orderRepository.findOrderForBusinessAdmin(orderId, businessIds)).thenReturn(Optional.of(order));

            // When
            OrderDto result = orderServiceImpl.findOrderForBusinessAdmin(orderId, businessIds);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should throw BusinessException when admin does not have permission")
        void findOrderForBusinessAdmin_shouldThrowException_whenAdminLacksPermission() {
            // Given
            String orderId = "order1";
            List<String> businessIds = List.of("biz2");
            when(orderRepository.findOrderForBusinessAdmin(orderId, businessIds)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderServiceImpl.findOrderForBusinessAdmin(orderId, businessIds))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Order not found or you do not have permission to view it.");
        }
    }

    @Nested
    @DisplayName("findByOrderIdWithAnyBusinessId tests")
    class FindByOrderIdWithAnyBusinessIdTests {

        @Test
        @DisplayName("Should return order when associated with any business ID")
        void findByOrderIdWithAnyBusinessId_shouldReturnOrder_whenAssociated() {
            // Given
            String orderId = "order1";
            when(orderRepository.findAnyByOrderId(orderId)).thenReturn(Optional.of(order));

            // When
            OrderDto result = orderServiceImpl.findByOrderIdWithAnyBusinessId(orderId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should throw BusinessException when not associated with any business ID")
        void findByOrderIdWithAnyBusinessId_shouldThrowException_whenNotAssociated() {
            // Given
            String orderId = "order1";
            when(orderRepository.findAnyByOrderId(orderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderServiceImpl.findByOrderIdWithAnyBusinessId(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Order not found or does not belong to any of the provided business IDs");
        }
    }

    @Nested
    @DisplayName("generateOrderId tests")
    class GenerateOrderIdTests {

        @Test
        @DisplayName("Should generate a valid order ID format")
        void generateOrderId_shouldGenerateValidFormat() {
            // When
            String orderId = orderServiceImpl.generateOrderId();

            // Then
            assertThat(orderId).isNotNull();
            assertThat(orderId).startsWith("ORD");
            assertThat(orderId.length()).isGreaterThan(10);
        }
    }
}