package com.clickstechnology.Brillo.Mall.domain.orders;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentMethod;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "BRILLO_ORDER",
        indexes = {
                @jakarta.persistence.Index(name = "idx_brillo_order_business", columnList = "business_id"),
                @jakarta.persistence.Index(name = "idx_brillo_order_customer", columnList = "customer_id"),
                @jakarta.persistence.Index(name = "idx_brillo_order_user", columnList = "user_id"),
                @jakarta.persistence.Index(name = "idx_brillo_order_status", columnList = "status")
        },
        uniqueConstraints = {
                @jakarta.persistence.UniqueConstraint(name = "uk_brillo_order_order_id", columnNames = {"order_id"})
        }
)
class Order extends JpaAuditor implements Serializable {

    @Column(name = "business_id", nullable = false)
    private String businessId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "user_id")
    private String userId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addOrderItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
       if (item != null) {
           items.add(item);
           item.setOrder(this);
       }
    }

    public void addOrderItems(List<OrderItem> orderItems) {

        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        if (items == null) {
            items = new ArrayList<>();
        }

        Map<String, OrderItem> existingItems = items.stream()
                .collect(Collectors.toMap(
                        OrderItem::getProductId,
                        Function.identity(),
                        (existing, duplicate) -> existing
                ));

        for (OrderItem newItem : orderItems) {

            newItem.setOrder(this);

            String productId = newItem.getProductId();

            OrderItem existing = existingItems.get(productId);

            if (existing != null) {
                // Increase quantity
                existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
            } else {
                // Add new item
                items.add(newItem);
                existingItems.put(productId, newItem);
            }
        }
    }

    public OrderDto dto(CustomerDto customerDto, BusinessDto businessDto) {
        return OrderDto.builder()
                .id(this.getOrderId())
                .businessId(this.businessId)
                .business(businessDto)
                .customer(customerDto)
                .status(this.status)
                .paymentMethod(this.paymentMethod)
                .totalAmount(this.totalAmount)
                .shippingAddress(this.shippingAddress)
                .items(this.items.stream().map(OrderItem::dto).toList())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

    public OrderDto dto() {
        return OrderDto.builder()
                .id(this.getOrderId())
                .businessId(this.businessId)
                .status(this.status)
                .userId(this.userId)
                .business(BusinessDto.builder().id(this.businessId).build())
                .paymentMethod(this.paymentMethod)
                .totalAmount(this.totalAmount)
                .shippingAddress(this.shippingAddress)
                .items(this.items.stream().map(OrderItem::dto).toList())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
