package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "BRILLO_BUSINESS_SERVICE",
        indexes = {
                @Index(name = "idx_service_business_id", columnList = "business_id"),
                @Index(name = "idx_service_slug", columnList = "slug"),
                @Index(name = "idx_service_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_business_service_slug",
                        columnNames = {"business_id", "slug"}
                )
        }
)
class BusinessService extends JpaAuditor implements Serializable {

    @Column(name = "business_id", nullable = false)
    private String businessId;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Column(name = "slug", length = 150, nullable = false)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", length = 50)
    private PricingType pricingType;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Builder.Default
    @Column(name = "negotiable")
    private boolean negotiable = false;

    @Builder.Default
    @Column(name = "requires_schedule")
    private boolean requiresSchedule = false;

    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private EntityStatus status = EntityStatus.ACTIVE;

    public BusinessServiceDto dto() {
        return BusinessServiceDto.builder()
                .id(this.getReference())
                .businessId(this.businessId)
                .name(this.name)
                .slug(this.slug)
                .description(this.description)
                .category(this.category)
                .pricingType(this.pricingType)
                .basePrice(this.basePrice)
                .durationMinutes(this.durationMinutes)
                .negotiable(this.negotiable)
                .requiresSchedule(this.requiresSchedule)
                .active(this.active)
                .status(this.status)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
