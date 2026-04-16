package com.clickstechnology.Brillo.Mall.application.features.orders;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
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
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;
    private final BusinessService businessService;
    private final ProductService productService;

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
        String username = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto user = userService.findByUsername(username);

        // Route based on user role
        if (user.getRoles().contains(UserRole.ADMIN.name())) {
            return findOrderForBusinessAdmin(user, orderId);
        } else {
            return findOrderForCustomer(user.getPhoneNumber(), orderId);
        }
    }

    private OrderDto findOrderForBusinessAdmin(UserDto user, String orderId) {
        // 1. Find all businesses owned by the admin user
        List<BusinessDto> businesses = businessService.findAllByOwnerId(user.getId());
        List<String> businessIds = businesses.stream().map(BusinessDto::getId).toList();

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
        // 1. Resolve the customer
        CustomerDto customer = customerService.findByPhoneOrEmail(ownerIdentifier);

        // 2. Fetch the order details
        OrderDto order = orderService.findOrderDetailsForCustomer(orderId, customer.getId());

        // 3. Enrich order items with full Product details
        enrichOrderProducts(order);

        // 4. Enrich order items with Business data
        Set<String> businessIds = order.getItems().stream()
                .map(item -> item.getProduct().getBusinessId())
                .collect(Collectors.toSet());

        List<BusinessDto> businesses = businessService.findAllByBusinessIds(businessIds);
        Map<String, BusinessDto> businessMap = businesses.stream()
                .collect(Collectors.toMap(BusinessDto::getId, Function.identity()));

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
            final String businessId
    ) {
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
        String username = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto user = userService.findByUsername(username);
        log.info(":::Logged in user: {}", user.getRoles());

        // Route based on user role
        if (user.getRoles().contains(UserRole.ADMIN.name())) {
            if (businessId != null) {
                return orderService.findAllByBusinessId(businessId, page, pageSize);
            } else {
                // Find all businesses for the admin user
                List<BusinessDto> businesses = businessService.findAllByOwnerId(user.getId());
                if (businesses.isEmpty()) {
                    return new PaginatedResponse<>(page, pageSize, 0, 0, false, false, Collections.emptyList());
                }
                List<String> businessIds = businesses.stream().map(BusinessDto::getId).toList();

                PaginatedResponse<OrderDto> businessOrders = orderService.findAllByBusinessIds(businessIds, page, pageSize);
                enrichCustomerDetail(businessOrders.getItems());
                return businessOrders;
            }
        }

        log.info(":::User is a USER or CUSTOMER");
        if (user.getRoles().contains(UserRole.CUSTOMER.name()) || user.getRoles().contains(UserRole.USER.name())) {
            CustomerDto customer = customerService.findByUserId(user.getId());
            if (customer == null) {
                throw new BusinessException("Customer could not be found for user: " + user.getPhoneNumber());
            }
            return orderService.findAllByCustomerId(customer.getId(), page, pageSize);
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
}