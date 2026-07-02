package com.clickstechnology.Brillo.Mall.domain.payments;

import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentLogDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brillo_payment_log", indexes = {
        @Index(name = "idx_brillo_payment_log_reference", columnList = "reference"),
        @Index(name = "idx_brillo_payment_log_business", columnList = "business_id"),
        @Index(name = "idx_brillo_payment_log_payable", columnList = "payable_id")
})
class PaymentLog extends JpaAuditor {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "business_id", nullable = false)
    private String businessId;

    @Column(name = "payment_reference", unique = true, nullable = false)
    private String paymentReference;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntityStatus status = EntityStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payable_type", nullable = false)
    private PayableType payableType;

    @Column(name = "payable_id", nullable = false)
    private String payableId;

    public PaymentLogDto dto() {
        return PaymentLogDto.builder()
                .id(reference)
                .businessId(businessId)
                .userId(userId)
                .paymentReference(paymentReference)
                .email(email)
                .amount(amount)
                .status(status)
                .payableType(payableType)
                .payableId(payableId)
                .build();
    }
}
