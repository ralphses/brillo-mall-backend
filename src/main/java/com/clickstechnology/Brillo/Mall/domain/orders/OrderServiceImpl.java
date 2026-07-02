package com.clickstechnology.Brillo.Mall.domain.orders;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.ResourceNotFoundException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class OrderServiceImpl implements OrderService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final CacheUtil cacheUtil;
    private final ProductService productService;
    private final BusinessService businessService;

    @Override
    public OrderDto findOrderById(String orderId) {
        return getOrder(orderId).dto();
    }

    @Override
    public OrderDto findOrderByIdAndCustomerId(String orderId, String customerId) {
        return orderRepository.findOrderDetailsByOrderIdAndCustomerId(orderId, customerId)
                .map(Order::dto)
                .orElseThrow(() -> new BusinessException("Order not found or does not belong to the customer"));
    }

    @Override
    public OrderDto findOrderDetailsForCustomer(String orderId, String customerId) {
        return orderRepository.findOrderDetailsByOrderIdAndCustomerId(orderId, customerId)
                .map(Order::dto)
                .orElseThrow(() -> new BusinessException("Order not found or does not belong to the customer."));
    }

    @Override
    public PaginatedResponse<OrderDto> findAllByUserId(String userId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Order> orderPage = orderRepository.findAllByUserId(userId, pageable);
        return getOrderPaginatedResponse(orderPage);
    }

    @Override
    public void ensureOrderBelongsToUser(OrderDto order, String userId) {
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("Order does not belong to the user.");
        }
    }

    @Override
    @Transactional
    public OrderDto addItemsToOrder(String existingOrderId, PlaceOrderRequest request) {

        Order order = getOrder(existingOrderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Order not valid or cancelled");
        }

        List<OrderItem> orderItems = createOrderItems(request);

        BigDecimal newItemsTotal = orderItems.stream()
                .map(item -> item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.addOrderItems(orderItems);
        order.setTotalAmount(order.getTotalAmount().add(newItemsTotal));
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCustomerId(request.getCustomer().getId());
        order.setShippingAddress(request.getCustomer().getAddress());

        return orderRepository.save(order).dto();
    }

    @Override
    public OrderDto createNewOrder(String newOrderId, PlaceOrderRequest request, String userId, String businessId) {

        List<OrderItem> orderItems = createOrderItems(request);

        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order newOrder = Order.builder()
                .orderId(newOrderId)
                .userId(userId)
                .businessId(businessId)
                .customerId(request.getCustomer().getId())
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getCustomer().getAddress())
                .totalAmount(totalAmount)
                .build();

        newOrder.addOrderItems(orderItems);

        return orderRepository.saveAndFlush(newOrder).dto();
    }

    @Override
    public String generateOrderId() {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String randomPart = String.format("%04d", new SecureRandom().nextInt(10000));
        return "ORD" + timestamp + randomPart;
    }

    @Override
    public OrderDto findOrderForBusinessAdmin(String orderId, List<String> businessIds) {
        return orderRepository.findOrderForBusinessAdmin(orderId, businessIds)
                .map(Order::dto)
                .orElseThrow(() -> new BusinessException("Order not found or you do not have permission to view it."));
    }

    @Override
    public OrderDto findByOrderIdWithAnyBusinessId(String orderId) {
        return orderRepository.findAnyByOrderId(orderId)
                .map(Order::dto)
                .orElseThrow(() -> new BusinessException("Order not found or does not belong to any of the provided business IDs"));
    }

    @Override
    public PaginatedResponse<OrderDto> findAllByCustomerId(String customerId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Order> orderPage = orderRepository.findAllByCustomerId(customerId, pageable);
        return getOrderPaginatedResponse(orderPage);
    }

    @Override
    public PaginatedResponse<OrderDto> findAllByBusinessId(String businessId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Order> orderPage = orderRepository.findAllByBusinessId(businessId, pageable);
        return getOrderPaginatedResponse(orderPage);
    }

    @Override
    public PaginatedResponse<OrderDto> findAllByBusinessIds(List<String> businessIds, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Order> orderPage = orderRepository.findAllByBusinessIds(businessIds, pageable);
        return getOrderPaginatedResponse(orderPage);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == newStatus) {
            return order.dto();
        }

        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.PENDING) {
                throw new BusinessException("Order not valid or cancelled");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return updatedOrder.dto();
    }

    private PaginatedResponse<OrderDto> getOrderPaginatedResponse(Page<Order> orderPage) {
        List<OrderDto> orderDtos = orderPage.getContent().stream().map(Order::dto).collect(Collectors.toList());

        if (orderDtos.isEmpty()) {
            return PaginatedResponse.<OrderDto>builder()
                    .items(orderDtos)
                    .page(orderPage.getNumber() + 1)
                    .perPage(orderPage.getSize())
                    .total(orderPage.getTotalElements())
                    .totalPages(orderPage.getTotalPages())
                    .hasNext(orderPage.hasNext())
                    .hasPrevious(orderPage.hasPrevious())
                    .build();
        }

        // Collect all product IDs from the current page of orders
        List<String> productIds = orderDtos.stream()
                .flatMap(orderDto -> orderDto.getItems().stream())
                .map(item -> item.getProduct().getId())
                .distinct()
                .toList();

        // Fetch all products and businesses in batch
        List<ProductDto> products = productService.findProductsByIds(productIds);
        Map<String, ProductDto> productMap = products.stream()
                .collect(Collectors.toMap(ProductDto::getId, product -> product));

        Map<String, String> orderCustomerMap = orderPage.getContent().stream()
                .collect(Collectors.toMap(Order::getOrderId, Order::getCustomerId));

        Set<String> businessIds = orderPage.getContent().stream()
                .map(Order::getBusinessId)
                .collect(Collectors.toSet());
        List<BusinessDto> businesses = businessIds.isEmpty()
                ? List.of()
                : businessService.findAllByBusinessIds(businessIds);
        Map<String, BusinessDto> businessMap = businesses.stream()
                .collect(Collectors.toMap(BusinessDto::getId, business -> business));

        // Enrich the order DTOs
        orderDtos.forEach(orderDto -> orderDto.getItems().forEach(item -> {
            ProductDto fullProduct = productMap.get(item.getProduct().getId());
            if (fullProduct != null) {
                item.setProduct(fullProduct);
                item.setBusiness(businessMap.get(fullProduct.getBusinessId()));
            }

            CustomerDto customerDto = CustomerDto.builder()
                    .id(orderCustomerMap.get(orderDto.getId()))
                    .build();

            orderDto.setCustomer(customerDto);
            orderDto.setBusiness(businessMap.get(orderDto.getBusinessId()));
        }));

        return PaginatedResponse.<OrderDto>builder()
                .items(orderDtos)
                .page(orderPage.getNumber() + 1)
                .perPage(orderPage.getSize())
                .total(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
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
