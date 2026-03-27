package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;

public interface OrderService {
    OrderDto findOrderById(String orderId);

    OrderDto addItemsToOrder(String id, PlaceOrderRequest request);

    OrderDto createNewOrder(String newOrderId, PlaceOrderRequest request);

    String generateOrderId();

}
