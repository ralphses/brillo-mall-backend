package com.clickstechnology.Brillo.Mall.application.dto.search;

import com.clickstechnology.Brillo.Mall.application.enums.PublicSearchResultType;
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
public class PublicSearchResultDto {

    private PublicSearchResultType type;
    private String id;
    private String name;
    private String description;
    private String category;
    private String businessId;
    private String businessName;
    private String slug;
    private String sku;
    private String imageUrl;
    private BigDecimal price;
    private String location;
    private Instant createdAt;
}
