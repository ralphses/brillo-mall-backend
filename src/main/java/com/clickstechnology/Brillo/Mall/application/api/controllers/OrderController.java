package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderPlacedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.enums.RequestSource;
import com.clickstechnology.Brillo.Mall.application.features.orders.ManageOrder;
import com.clickstechnology.Brillo.Mall.application.features.orders.PlaceOrder;
import com.clickstechnology.Brillo.Mall.application.dto.order.UpdateOrderStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/orders")
@Tag(name = "Orders", description = "Order placement, retrieval, and lifecycle APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final PlaceOrder placeOrder;
    private final ManageOrder manageOrder;

    @PostMapping
    @Operation(summary = "Place order")
    public ResponseWrapper<OrderPlacedResponse> placeOrder(
            @RequestBody @Valid final PlaceOrderRequest request,
            final HttpServletRequest httpServletRequest) {
        OrderPlacedResponse response = placeOrder.execute(request, httpServletRequest);
        return success(response);
    }

    @GetMapping("{orderId}")
    @Operation(summary = "Get order by id")
    public ResponseWrapper<OrderDto> getOrder(
            @PathVariable String orderId,
            final HttpServletRequest httpServletRequest) {
        OrderDto order = manageOrder.findOrderById(orderId, RequestSource.API, httpServletRequest, null);
        return success(order);
    }

    @GetMapping
    @Operation(summary = "List orders")
    public ResponseWrapper<PaginatedResponse<OrderDto>> getAllOrders(
            final HttpServletRequest httpServletRequest,
            @RequestParam(value = "page", defaultValue = "1") final Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20")  final Integer pageSize,
            @RequestParam(value = "businessId", required = false) final String businessId,
            @RequestParam(value = "isBusiness", required = false, defaultValue = "false") final boolean isBusiness,
            @RequestParam(value = "ownerId", required = false) final String ownerId
            ) {
        return success(manageOrder.findAllOrders(RequestSource.API, httpServletRequest, page, pageSize, ownerId, businessId, isBusiness));
    }

    @PutMapping("{orderId}/status")
    @Operation(summary = "Update order status")
    public ResponseWrapper<OrderDto> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody @Valid final UpdateOrderStatusRequest request,
            final HttpServletRequest httpServletRequest) {
        OrderDto order = manageOrder.updateOrder(orderId, request, httpServletRequest);
        return success(order);
    }

    @PostMapping("{orderId}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseWrapper<OrderDto> cancelOrder(
            @PathVariable String orderId,
            final HttpServletRequest httpServletRequest) {
        OrderDto order = manageOrder.cancelOrder(orderId, httpServletRequest);
        return success(order);
    }
}
