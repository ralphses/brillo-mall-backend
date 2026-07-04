package com.clickstechnology.Brillo.Mall.application.dto.product;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private String id;
    private String businessId;
    private String name;
    private String category;
    private String description;
    private String mainImage;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private String sku;
    private boolean flashSale;
    private Integer quantity;
    private EntityStatus status;
    private Instant createdAt;
    private Instant updatedAt;

}
