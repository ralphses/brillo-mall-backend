package com.clickstechnology.Brillo.Mall.application.features.orders;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemDto;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceOrder {
    private final OrderService orderService;
    private final BusinessService businessService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final AuthenticationUtil authenticationUtil;

    @LoggableRequest
    public OrderPlacedResponse execute(PlaceOrderRequest request, HttpServletRequest httpServletRequest) {

        String userId = resolveUserId(httpServletRequest, request.getCustomer());

        List<ProductDto> products = loadProducts(request);
        Set<String> businessIds = products.stream()
                .map(ProductDto::getBusinessId)
                .collect(Collectors.toSet());
        String businessId = validateProductsAndGetBusinessId(products);

        CustomerDto customer = resolveCustomer(request, userId, businessIds);

        validateInventory(request.getItems());

        OrderDto order = createOrUpdateOrder(request, userId, businessId);

        attachCustomerToBusinesses(customer, products);

        enrichOrderDto(order);

        return new OrderPlacedResponse("New order placed successfully", order);
    }

    private CustomerDto resolveCustomer(PlaceOrderRequest request, String userId, Set<String> businessIds) {
        CustomerDto thisCustomer = customerService.resolveCustomer(request.getCustomer(), userId, businessIds);
        request.setCustomer(thisCustomer);
        return thisCustomer;
    }

    private String resolveUserId(final HttpServletRequest httpServletRequest, final CustomerDto customer) {
        return authenticationUtil.getAuthenticatedUsername(httpServletRequest);
    }

    private void enrichOrderDto(final OrderDto order) {

        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        // Collect product IDs
        Set<String> productIds = order.getItems().stream()
                .map(OrderItemDto::getProduct)
                .filter(Objects::nonNull)
                .map(ProductDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) {
            return;
        }

        Map<String, ProductDto> productMap =
                productService.findAllByProductIds(productIds).stream()
                        .collect(Collectors.toMap(
                                ProductDto::getId,
                                Function.identity()
                        ));

        order.getItems().forEach(item -> {
            ProductDto product = item.getProduct();
            if (product != null) {
                ProductDto enriched = productMap.get(product.getId());
                if (enriched != null) {
                    item.setProduct(enriched);
                }
            }
        });
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

    private OrderDto createOrUpdateOrder(PlaceOrderRequest request, String userId, String businessId) {

        if (request.getOrderId() != null) {
            OrderDto existingOrder =
                    orderService.findOrderById(request.getOrderId());

            if (existingOrder.getBusinessId() != null && !existingOrder.getBusinessId().equals(businessId)) {
                throw new BusinessException("Order items must belong to the same business.");
            }

            return orderService.addItemsToOrder(
                    existingOrder.getId(),
                    request
            );
        }

        String orderId = orderService.generateOrderId();

        return orderService.createNewOrder(orderId, request, userId, businessId);
    }

    private void attachCustomerToBusinesses(
            CustomerDto customer,
            List<ProductDto> products) {

        Set<String> businessIds = products.stream()
                .map(ProductDto::getBusinessId)
                .collect(Collectors.toSet());

        businessService.addCustomer(customer, businessIds);
    }

    private String validateProductsAndGetBusinessId(List<ProductDto> products) {
        if (products.isEmpty()) {
            throw new BusinessException("No products found for the given IDs.");
        }

        Set<String> allProductOwners = products.stream()
                .map(ProductDto::getBusinessId)
                .collect(Collectors.toSet());

        if (allProductOwners.size() != 1) {
            throw new BusinessException("Order items must belong to the same business.");
        }

        businessService.validateBusinessIsActive(allProductOwners);
        return allProductOwners.iterator().next();
    }
}
