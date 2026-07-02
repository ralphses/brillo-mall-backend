package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.BookingStatus;
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
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status <> 'DELETED'")
@Table(
        name = "brillo_booked_business_service",
        indexes = {
                @Index(name = "idx_brillo_booked_business_service_business", columnList = "business_id"),
                @Index(name = "idx_brillo_booked_business_service_service", columnList = "business_service_id"),
                @Index(name = "idx_brillo_booked_business_service_user", columnList = "user_id"),
                @Index(name = "idx_brillo_booked_business_service_customer", columnList = "customer_id"),
                @Index(name = "idx_brillo_booked_business_service_status", columnList = "booking_status"),
                @Index(name = "idx_brillo_booked_business_service_request", columnList = "service_request_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_brillo_booked_business_service_request",
                        columnNames = {"service_request_id"}
                )
        }
)
class BookedBusinessService extends JpaAuditor implements Serializable {

    @Column(name = "business_id", nullable = false)
    private String businessId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "business_service_id", nullable = false)
    private String businessServiceId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "service_request_id")
    private String serviceRequestId;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "agreed_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal agreedPrice;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", length = 50, nullable = false)
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public BookedServiceDto dto() {
        return BookedServiceDto.builder()
                .id(reference)
                .business(BusinessDto.builder().id(businessId).build())
                .user(UserDto.builder().id(userId).build())
                .customer(CustomerDto.builder().id(customerId).build())
                .businessService(BusinessServiceDto.builder().id(businessServiceId).build())
                .businessServiceRequest(BusinessServiceRequestDto.builder().id(serviceRequestId).build())
                .agreedPrice(agreedPrice)
                .scheduledDate(scheduledDate)
                .location(location)
                .bookingStatus(bookingStatus)
                .status(status)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
