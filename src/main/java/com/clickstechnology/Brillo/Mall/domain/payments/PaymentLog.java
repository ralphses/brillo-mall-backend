package com.clickstechnology.Brillo.Mall.domain.payments;

import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentLogDto;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;
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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brillo_payment_log", indexes = {
        @Index(name = "idx_brillo_payment_log_reference", columnList = "reference"),
        @Index(name = "idx_brillo_payment_log_business", columnList = "business_id"),
        @Index(name = "idx_brillo_payment_log_payable", columnList = "payable_id"),
        @Index(name = "idx_brillo_payment_log_payment_status", columnList = "payment_status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_brillo_payment_log_payable", columnNames = {"payable_type", "payable_id"})
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
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "authorization_url")
    private String authorizationUrl;

    @Column(name = "access_code")
    private String accessCode;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "reconciled_at")
    private Instant reconciledAt;

    @Column(name = "gateway_message")
    private String gatewayMessage;

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
                .paymentStatus(paymentStatus)
                .authorizationUrl(authorizationUrl)
                .accessCode(accessCode)
                .verifiedAt(verifiedAt)
                .reconciledAt(reconciledAt)
                .gatewayMessage(gatewayMessage)
                .payableType(payableType)
                .payableId(payableId)
                .build();
    }
}
