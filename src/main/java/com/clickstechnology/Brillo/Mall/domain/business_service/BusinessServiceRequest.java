package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@Table(name = "BRILLO_BUSINESS_SERVICE_REQUEST")
class BusinessServiceRequest extends JpaAuditor implements Serializable {

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "business_id", length = 100)
    private String businessId;

    @Column(name = "whatsapp_conversation_id", length = 100)
    private String whatsappConversationId;

    @Column(name = "business_service_id", nullable = false)
    private String businessServiceId;

    @Column(name = "initial_price", precision = 19, scale = 2)
    private BigDecimal initialPrice;

    @Column(name = "last_offered_price", precision = 19, scale = 2)
    private BigDecimal lastOfferedPrice;

    @Column(name = "agreed_price", precision = 19, scale = 2)
    private BigDecimal agreedPrice;

    @Column(name = "negotiation_attempts")
    private Integer negotiationAttempts = 1;

    @Column(name = "human_takeover")
    private boolean humanTakeover = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private EntityStatus status = EntityStatus.PENDING;

    public BusinessServiceRequestDto dto() {
        return BusinessServiceRequestDto.builder()
                .id(reference)
                .businessService(BusinessServiceDto.builder()
                        .id(businessServiceId)
                        .build())
                .initialPrice(initialPrice)
                .lastOfferedPrice(lastOfferedPrice)
                .agreedPrice(agreedPrice)
                .negotiationAttempts(negotiationAttempts)
                .humanTakeover(humanTakeover)
                .notes(notes)
                .status(status)
                .whatsappConversationId(whatsappConversationId)
                .user(UserDto.builder()
                        .id(userId)
                        .build())
                .business(BusinessDto.builder()
                        .id(businessId)
                        .build())
                .build();
    }
}
