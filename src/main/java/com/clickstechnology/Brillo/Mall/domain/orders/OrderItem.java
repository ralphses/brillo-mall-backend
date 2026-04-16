package com.clickstechnology.Brillo.Mall.domain.orders;

import com.clickstechnology.Brillo.Mall.application.dto.order.OrderItemDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BRILLO_ORDER_ITEM")
class OrderItem extends JpaAuditor implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @JoinColumn(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_purchase", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAtPurchase;

    public OrderItemDto dto() {
        return OrderItemDto.builder()
                .id(this.getReference())
                .product(ProductDto.builder()
                        .id(this.getProductId())
                        .build())
                .quantity(this.quantity)
                .priceAtPurchase(this.priceAtPurchase)
                .build();
    }

    public OrderItemDto dto(ProductDto productDto) {
        return OrderItemDto.builder()
                .id(this.getReference())
                .product(productDto)
                .quantity(this.quantity)
                .priceAtPurchase(this.priceAtPurchase)
                .build();
    }
}
