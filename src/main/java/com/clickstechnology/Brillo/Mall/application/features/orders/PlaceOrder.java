package com.clickstechnology.Brillo.Mall.application.features.orders;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderPlacedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemRequest;
import com.clickstechnology.Brillo.Mall.application.dto.order.PlaceOrderRequest;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceOrder {
    private final OrderService orderService;
    private final BusinessService businessService;
    private final ProductService productService;
    private final CustomerService customerService;

    @LoggableRequest
    public OrderPlacedResponse execute(PlaceOrderRequest request, HttpServletRequest httpServletRequest) {

        CustomerDto customer = customerService.resolveCustomer(request.getCustomer());

        List<ProductDto> products = loadProducts(request);

        validateProductsAndGetBusinessId(products);

        validateInventory(request.getItems());

        OrderDto order = createOrUpdateOrder(request);

        attachCustomerToBusinesses(customer, products);

        return new OrderPlacedResponse("New order placed successfully", order);
    }

    private List<ProductDto> loadProducts(PlaceOrderRequest request) {

        List<String> productIds = request.getItems()
                .stream()
                .map(OrderItemRequest::getProductId)
                .toList();

        return productService.findProductsByIds(productIds);
    }

    private void validateInventory(List<OrderItemRequest> items) {

        Map<String, Integer> productQuantityMap = items.stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getProductId,
                        OrderItemRequest::getQuantity)
                );

        productService.checkInStock(productQuantityMap);
    }

    private OrderDto createOrUpdateOrder(PlaceOrderRequest request) {

        if (request.getOrderId() != null) {
            OrderDto existingOrder =
                    orderService.findOrderById(request.getOrderId());

            return orderService.addItemsToOrder(
                    existingOrder.getId(),
                    request
            );
        }

        String orderId = orderService.generateOrderId();

        return orderService.createNewOrder(orderId, request);
    }

    private void attachCustomerToBusinesses(
            CustomerDto customer,
            List<ProductDto> products) {

        Set<String> businessIds = products.stream()
                .map(ProductDto::getBusinessId)
                .collect(Collectors.toSet());

        businessService.addCustomer(customer, businessIds);
    }

    private void validateProductsAndGetBusinessId(List<ProductDto> products) {
        if (products.isEmpty()) {
            throw new BusinessException("No products found for the given IDs.");
        }

        Set<String> productIds = products.stream().map(ProductDto::getId).collect(Collectors.toSet());
        List<ProductDto> allProducts = productService.findAllByProductIds(productIds);

        Set<String> allProductOwners = allProducts.stream()
                .map(ProductDto::getBusinessId).collect(Collectors.toSet());

        businessService.validateBusinessIsActive(allProductOwners);
    }
}