package com.clickstechnology.Brillo.Mall.application.dto.request.product;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String name;

    private String description;

    @Positive(message = "Price must be positive.")
    private BigDecimal price;

    @Positive(message = "Discounted price must be positive.")
    private BigDecimal discountedPrice;

    private String sku;

    private Integer quantity;
}
