package com.clickstechnology.Brillo.Mall.domain.product;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status <> 'DELETED'")
@Table(
        name = "BRILLO_PRODUCT",
        indexes = {
                @Index(name = "idx_brillo_product_business", columnList = "business_id"),
                @Index(name = "idx_brillo_product_status", columnList = "status")
        },
        uniqueConstraints = {
                @jakarta.persistence.UniqueConstraint(name = "uk_brillo_product_business_sku", columnNames = {"business_id", "sku"}),
                @jakarta.persistence.UniqueConstraint(name = "uk_brillo_product_business_name", columnNames = {"business_id", "name"})
        }
)
class Product extends JpaAuditor implements Serializable {

    @Column(name = "business_id", length = 100, nullable = false)
    private String businessId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "sku", length = 50)
    private String sku;

    @Column(name = "main_image_url", length = 200)
    private String mainImageUrl;

    @Column(name = "quantity")
    private Integer quantity;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 100)
    private EntityStatus status = EntityStatus.ACTIVE;

    public ProductDto dto() {
        return ProductDto.builder()
                .id(this.getReference())
                .businessId(this.businessId)
                .name(this.name)
                .mainImage(mainImageUrl)
                .description(this.description)
                .price(this.price)
                .discountedPrice(this.discountedPrice)
                .sku(this.sku)
                .quantity(this.quantity)
                .status(this.status)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

}
