package com.clickstechnology.Brillo.Mall.domain.business;


import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
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

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status <> 'DELETED'")
@Table(
        name = "BRILLO_BUSINESS",
        indexes = {
                @Index(name = "idx_brillo_business_owner", columnList = "owner_id"),
                @Index(name = "idx_brillo_business_slug", columnList = "slug"),
                @Index(name = "idx_brillo_business_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_brillo_business_slug", columnNames = {"slug"}),
                @UniqueConstraint(name = "uk_brillo_business_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uk_brillo_business_phone", columnNames = {"phone_number"})
        }
)
class Business extends JpaAuditor implements Serializable {

    // points to the reference of user
    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "slug", length = 120, nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private BusinessCategory category;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 100)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "whatsapp_type", length = 50)
    @Builder.Default
    private WhatsappType whatsappType = WhatsappType.SHARED;

    @Column(name = "whatsapp_number", length = 20)
    private String whatsappNumber;

    @Column(name = "storefront_name", length = 100)
    @Builder.Default
    private String storefrontName = "Default";

    @Column(name = "storefront_active")
    @Builder.Default
    private Boolean storefrontActive = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "setup_completed")
    @Builder.Default
    private Boolean setupCompleted = false;

    public BusinessDto dto() {
        return BusinessDto.builder()
                .id(this.getReference())
                .ownerId(this.ownerId)
                .name(this.name)
                .slug(this.slug)
                .category(this.category)
                .status(this.status)
                .description(this.description)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .logoUrl(this.logoUrl)
                .state(this.state)
                .city(this.city)
                .address(this.address)
                .whatsappType(this.whatsappType)
                .whatsappNumber(this.whatsappNumber)
                .storefrontName(this.storefrontName)
                .storefrontActive(this.storefrontActive)
                .isActive(this.isActive)
                .setupCompleted(this.setupCompleted)
                .build();
    }
}