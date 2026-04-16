package com.clickstechnology.Brillo.Mall.application.dto.order;

import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemDto {
    private String id;
    private ProductDto product;
    private BusinessDto business;
    private int quantity;
    private BigDecimal priceAtPurchase;
}