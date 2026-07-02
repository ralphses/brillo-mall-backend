package com.clickstechnology.Brillo.Mall.domain.customer;


import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status <> 'DELETED'")
@Table(
        name = "BRILLO_CUSTOMER",
        indexes = {
                @Index(name = "idx_brillo_customer_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @jakarta.persistence.UniqueConstraint(name = "uk_brillo_customer_email", columnNames = {"email"}),
                @jakarta.persistence.UniqueConstraint(name = "uk_brillo_customer_phone", columnNames = {"phone_number"})
        }
)
class Customer extends JpaAuditor implements Serializable {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "customer_related_business_ids",
            joinColumns = @JoinColumn(name = "customer_id")
    )
    @Builder.Default
    @Column(name = "business_id")
    private Set<String> relatedBusinessIds = new HashSet<>();

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "full_name", length = 100, nullable = false)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 100)
    private EntityStatus status = EntityStatus.ACTIVE;

    public CustomerDto dto() {
        return CustomerDto.builder()
                .id(reference)
                .userId(userId)
                .customerName(name)
                .customerEmail(email)
                .customerPhoneNumber(phone)
                .address(address)
                .build();
    }
}
