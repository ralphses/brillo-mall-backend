package com.clickstechnology.Brillo.Mall.domain.publicsearch;

import com.clickstechnology.Brillo.Mall.application.dto.search.PublicSearchResultDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PublicSearchResultType;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "brillo_public_search_index",
        indexes = {
                @Index(name = "idx_public_search_visibility", columnList = "status,is_active,created_at"),
                @Index(name = "idx_public_search_type", columnList = "item_type"),
                @Index(name = "idx_public_search_business", columnList = "business_id"),
                @Index(name = "idx_public_search_category", columnList = "category_code"),
                @Index(name = "idx_public_search_slug", columnList = "slug"),
                @Index(name = "idx_public_search_sku", columnList = "sku")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_public_search_reference_type", columnNames = {"reference", "item_type"})
        }
)
public class PublicSearchIndex extends JpaAuditor implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 20, nullable = false)
    private PublicSearchResultType type;

    @Column(name = "business_id", length = 36)
    private String businessId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_code", length = 100)
    private String categoryCode;

    @Column(name = "category_label", length = 150)
    private String categoryLabel;

    @Column(name = "business_name", length = 150)
    private String businessName;

    @Column(name = "slug", length = 150)
    private String slug;

    @Column(name = "sku", length = 50)
    private String sku;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "price", precision = 19, scale = 2)
    private BigDecimal price;

    @Lob
    @Column(name = "search_text", nullable = false, columnDefinition = "LONGTEXT")
    private String searchText;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private EntityStatus status = EntityStatus.ACTIVE;

    public PublicSearchResultDto dto() {
        String resolvedCategory = categoryLabel != null ? categoryLabel : categoryCode;
        return PublicSearchResultDto.builder()
                .type(type)
                .id(getReference())
                .name(name)
                .description(description)
                .category(type == PublicSearchResultType.BUSINESS ? categoryCode : resolvedCategory)
                .businessId(businessId)
                .businessName(businessName)
                .slug(slug)
                .sku(sku)
                .imageUrl(imageUrl)
                .price(price)
                .location(location)
                .createdAt(getCreatedAt())
                .build();
    }
}
