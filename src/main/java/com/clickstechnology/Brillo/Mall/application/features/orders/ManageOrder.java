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
import com.clickstechnology.Brillo.Mall.application.dto.order.UpdateOrderStatusRequest;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.enums.RequestSource;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageOrder {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final BusinessService businessService;
    private final ProductService productService;
    private final TenantContextResolver tenantContextResolver;

    public OrderDto findOrderById(
            final String orderId,
            final RequestSource source,
            final HttpServletRequest httpServletRequest,
            final String ownerId) {

        // Handle WhatsApp requests and owner ID is phone number
        if (source == RequestSource.WHATSAPP && ownerId != null) {
            return findOrderForCustomer(ownerId, orderId);
        }

        // Handle Web requests
        UserDto user = tenantContextResolver.currentUser(httpServletRequest);

        // Route based on user role
        if (user.getRoles().contains(UserRole.ADMIN.name())) {
            return findOrderForBusinessAdmin(httpServletRequest, orderId);
        } else {
            return findOrderForCustomer(user.getPhoneNumber(), orderId);
        }
    }

    private OrderDto findOrderForBusinessAdmin(HttpServletRequest httpServletRequest, String orderId) {
        // 1. Find all businesses owned by the admin user
        List<String> businessIds = tenantContextResolver.ownedBusinessIds(httpServletRequest);

        if (businessIds.isEmpty()) {
            throw new BusinessException("Admin user does not own any businesses.");
        }

        // 2. Fetch the order, but only with items belonging to the admin's businesses
        OrderDto order = orderService.findOrderForBusinessAdmin(orderId, businessIds);

        // 3. Enrich the order items with full product and business details
        enrichOrderProducts(order);

        return order;
    }

    private void enrichOrderProducts(OrderDto order) {
        List<String> productIds = order.getItems().stream()
                .map(item -> item.getProduct().getId())
                .toList();

        List<ProductDto> products = productService.findProductsByIds(productIds);
        Map<String, ProductDto> productMap = products.stream()
                .collect(Collectors.toMap(ProductDto::getId, Function.identity()));

        order.getItems().forEach(item -> {
            ProductDto product = productMap.get(item.getProduct().getId());
            if (product != null) {
                item.setProduct(product);
            }
        });
    }


    private OrderDto findOrderForCustomer(String ownerIdentifier, String orderId) {

        // 2. Fetch the order details
        OrderDto order = orderService.findOrderById(orderId);

        // 3. Enrich order items with full Product details
        enrichOrderProducts(order);

        // 4. Enrich order and items with Business data
        Set<String> businessIds = order.getBusinessId() != null
                ? Set.of(order.getBusinessId())
                : order.getItems().stream()
                .map(item -> item.getProduct().getBusinessId())
                .collect(Collectors.toSet());

        List<BusinessDto> businesses = businessService.findAllByBusinessIds(businessIds);
        Map<String, BusinessDto> businessMap = businesses.stream()
                .collect(Collectors.toMap(BusinessDto::getId, Function.identity()));

        order.setBusiness(businessMap.get(order.getBusinessId()));
        order.getItems().forEach(item -> {
            BusinessDto business = businessMap.get(item.getProduct().getBusinessId());
            if (business != null) {
                item.setBusiness(business);
            }
        });

        return order;
    }

    public PaginatedResponse<OrderDto> findAllOrders(
            final RequestSource source,
            final HttpServletRequest httpServletRequest,
            final Integer page,
            final Integer pageSize,
            final String ownerId,
            final String businessId,
            boolean isBusiness) {

        // Handle WhatsApp requests
        if (source == RequestSource.WHATSAPP && ownerId != null) {
            CustomerDto customer = customerService.findByPhoneOrEmail(ownerId);
            if (customer == null) {
                throw new BusinessException("Customer could not be found for ownerId: " + ownerId);
            }
            return orderService.findAllByCustomerId(customer.getId(), page, pageSize);
        }

        log.info("Request source {}, page: {}, pageSize: {}", source, page, pageSize);

        // Handle Web/API requests
        UserDto user = tenantContextResolver.currentUser(httpServletRequest);
        log.info(":::Logged in user: {}", user.getRoles());

        // Route based on a user role
        if (user.getRoles().contains(UserRole.ADMIN.name()) && (isBusiness || businessId != null)) {
            if (businessId != null) {
                tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
                return orderService.findAllByBusinessId(businessId, page, pageSize);
            } else {
                // Find all businesses for the admin user
                List<String> businessIds = tenantContextResolver.ownedBusinessIds(httpServletRequest);
                if (businessIds.isEmpty()) {
                    return new PaginatedResponse<>(page, pageSize, 0, 0, false, false, Collections.emptyList());
                }
                PaginatedResponse<OrderDto> businessOrders = orderService.findAllByBusinessIds(businessIds, page, pageSize);
                enrichCustomerDetail(businessOrders.getItems());
                return businessOrders;
            }
        }

        // Fetch user orders
        if (user.getRoles().contains(UserRole.USER.name())) {
            return orderService.findAllByUserId(user.getId(), page, pageSize);
        }

        // Default case if no role matches
        return new PaginatedResponse<>(page, pageSize, 0, 0, false, false, Collections.emptyList());
    }

    private void enrichCustomerDetail(List<OrderDto> items) {
        Set<String> customerIds = items.stream().map(OrderDto::getCustomer)
                .map(CustomerDto::getId)
                .collect(Collectors.toSet());

        Set<CustomerDto> customers = customerService.findAllByRefs(customerIds);
        Map<String, CustomerDto> customerMap = customers.stream().collect(Collectors.toMap(CustomerDto::getId, Function.identity()));
        items.forEach(item -> item.setCustomer(customerMap.get(item.getCustomer().getId())));
    }

    public OrderDto updateOrder(String orderId, UpdateOrderStatusRequest request, HttpServletRequest httpServletRequest) {
        UserDto user = tenantContextResolver.currentUser(httpServletRequest);

        List<String> businessIds = tenantContextResolver.ownedBusinessIds(httpServletRequest);

        if (businessIds.isEmpty()) {
            throw new BusinessException("User is not associated with any business.");
        }

        OrderDto order = orderService.findOrderForBusinessAdmin(orderId, businessIds);

        if (order == null) {
            throw new BusinessException("Order not found or user does not have permission to update it.");
        }

        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        return orderService.updateOrderStatus(orderId, newStatus);
    }

    public OrderDto cancelOrder(String orderId, HttpServletRequest httpServletRequest) {
        UserDto user = tenantContextResolver.currentUser(httpServletRequest);

        OrderDto order = orderService.findOrderById(orderId);

        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("You are not allowed to cancel this order.");
        }

        return orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }
}
