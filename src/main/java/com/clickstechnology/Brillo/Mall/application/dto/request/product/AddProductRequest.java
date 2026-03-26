package com.clickstechnology.Brillo.Mall.application.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductRequest {

    @NotBlank(message = "Product name cannot be blank.")
    private String name;

    private String description;

    @NotNull(message = "Price cannot be null.")
    @Positive(message = "Price must be positive.")
    private BigDecimal price;

    @Positive(message = "Discounted price must be positive.")
    private BigDecimal discountedPrice;

    private String sku;

    @NotNull(message = "Quantity cannot be null.")
    private Integer quantity;
}