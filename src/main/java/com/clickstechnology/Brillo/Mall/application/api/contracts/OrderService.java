package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;

import java.util.List;

public interface OrderService {
    OrderDto findOrderById(String orderId);

    OrderDto addItemsToOrder(String id, PlaceOrderRequest request);

    OrderDto createNewOrder(String newOrderId, PlaceOrderRequest request);

    String generateOrderId();

    OrderDto findOrderByIdAndCustomerId(String orderId, String customerId);

    OrderDto findByOrderIdWithAnyBusinessId(String orderId);

    PaginatedResponse<OrderDto> findAllByCustomerId(String id, Integer page, Integer pageSize);

    PaginatedResponse<OrderDto> findAllByBusinessId(String businessId, Integer page, Integer pageSize);

    PaginatedResponse<OrderDto> findAllByBusinessIds(List<String> businessIds, Integer page, Integer pageSize);

    OrderDto findOrderForBusinessAdmin(String orderId, List<String> businessIds);

    OrderDto findOrderDetailsForCustomer(String orderId, String id);
}