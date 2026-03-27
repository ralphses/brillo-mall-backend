package com.clickstechnology.Brillo.Mall.domain.orders;

import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class OrderServiceImpl implements OrderService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final CacheUtil cacheUtil;

    @Override
    public OrderDto findOrderById(String orderId) {
        return getOrder(orderId).dto();
    }

    @Override
    @Transactional
    public OrderDto addItemsToOrder(String existingOrderId, PlaceOrderRequest request) {

        Order order = getOrder(existingOrderId);

        List<OrderItem> orderItems = createOrderItems(request);

        BigDecimal newItemsTotal = orderItems.stream()
                .map(item -> item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.addOrderItems(orderItems);
        order.setTotalAmount(order.getTotalAmount().add(newItemsTotal));
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCustomerId(request.getCustomer().getId());
        order.setShippingAddress(order.getShippingAddress());

        return orderRepository.save(order).dto();
    }

    @Override
    public OrderDto createNewOrder(String newOrderId, PlaceOrderRequest request) {

        List<OrderItem> orderItems = createOrderItems(request);

        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order newOrder = Order.builder()
                .orderId(newOrderId)
                .customerId(request.getCustomer().getId())
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getCustomer().getAddress())
                .totalAmount(totalAmount)
                .build();

        newOrder.addOrderItems(orderItems);

        return orderRepository.save(newOrder).dto();
    }

    @Override
    public String generateOrderId() {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String randomPart = String.format("%04d", new SecureRandom().nextInt(10000));
        return "ORD" + timestamp + randomPart;
    }

    private Order getOrder(String orderId) {
        String cacheKey = CacheNames.ORDER_ID + orderId;
        return Optional.ofNullable(cacheUtil.get(cacheKey, Order.class))
                .orElseGet(() -> orderRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new BusinessException("Order with ID: " + orderId + " does not exist")));
    }

    private static List<OrderItem> createOrderItems(PlaceOrderRequest request) {
        return request.getItems()
                .stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

}