package com.clickstechnology.Brillo.Mall.domain.orders;

import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
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

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BRILLO_ORDER")
class Order extends JpaAuditor implements Serializable {

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

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
        items.add(item);
        item.setOrder(this);
    }

    public void addOrderItems(List<OrderItem> orderItems) {
        if (items == null) {
            items = new ArrayList<>();
        }

        if (orderItems != null && !orderItems.isEmpty()) {
            orderItems.forEach(item -> item.setOrder(this));
            items.addAll(orderItems);
        }

    }

    public OrderDto dto(UserDto customerDto, BusinessDto businessDto) {
        return OrderDto.builder()
                .id(this.getOrderId())
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
                .status(this.status)
                .paymentMethod(this.paymentMethod)
                .totalAmount(this.totalAmount)
                .shippingAddress(this.shippingAddress)
                .items(this.items.stream().map(OrderItem::dto).toList())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }


}