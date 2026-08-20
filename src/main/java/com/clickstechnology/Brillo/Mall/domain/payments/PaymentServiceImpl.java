package com.clickstechnology.Brillo.Mall.domain.payments;

import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentService;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentLogDto;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor.RecordStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentService {
    private final PaymentLogRepository paymentLogRepository;

    @Override
    public void createNewLog(String businessId, String userId, String paymentReference, BigDecimal amount, PayableType payableType, String payableId, String email) {
        PaymentLog paymentLog = PaymentLog.builder()
                .businessId(businessId)
                .userId(userId)
                .paymentReference(paymentReference)
                .amount(amount)
                .payableType(payableType)
                .payableId(payableId)
                .email(email)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        paymentLogRepository.save(paymentLog);
    }

    @Transactional
    @Override
    public void updateInitialization(String paymentLogId, String authorizationUrl, String accessCode, PaymentStatus status) {
        PaymentLog existingPaymentLog = findByPaymentReferenceInternal(paymentLogId);
        existingPaymentLog.setAuthorizationUrl(authorizationUrl);
        existingPaymentLog.setAccessCode(accessCode);
        existingPaymentLog.setPaymentStatus(status);
        existingPaymentLog.setVerifiedAt(null);
        existingPaymentLog.setReconciledAt(null);
        existingPaymentLog.setGatewayMessage(null);
        paymentLogRepository.save(existingPaymentLog);
    }

    @Transactional
    @Override
    public void updateStatus(String paymentLogId, PaymentStatus status) {
        PaymentLog existingPaymentLog = findByPaymentReferenceInternal(paymentLogId);
        applyStatusTransition(existingPaymentLog, status);
        paymentLogRepository.save(existingPaymentLog);
    }

    @Override
    public void updateStatusWithPaymentReference(String reference, PaymentStatus status) {
        PaymentLog paymentLog = findByPaymentReferenceInternal(reference);
        applyStatusTransition(paymentLog, status);
        paymentLogRepository.save(paymentLog);
    }

    private void applyStatusTransition(PaymentLog paymentLog, PaymentStatus status) {
        PaymentStatus currentStatus = paymentLog.getPaymentStatus();
        if (currentStatus == status) {
            return;
        }

        if (!currentStatus.canTransitionTo(status)) {
            throw new BusinessException("Invalid payment status transition.");
        }

        paymentLog.setPaymentStatus(status);
        paymentLog.setGatewayMessage(status.name());

        Instant now = Instant.now();
        if (status == PaymentStatus.PAID) {
            paymentLog.setVerifiedAt(paymentLog.getVerifiedAt() == null ? now : paymentLog.getVerifiedAt());
            paymentLog.setReconciledAt(now);
        } else if (status == PaymentStatus.FAILED || status == PaymentStatus.REVERSED) {
            paymentLog.setReconciledAt(now);
        }
    }

    private PaymentLog findByPaymentReferenceInternal(String reference) {
        return paymentLogRepository.findByPaymentReferenceAndRecordStatus(reference, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Payment log not found"));
    }

    private PaymentLog findByReference(String paymentLogId) {
        return paymentLogRepository.findByReferenceAndRecordStatus(paymentLogId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Payment log not found"));
    }

    @Override
    public PaymentLogDto findByPaymentReference(String reference) {
        return findByPaymentReferenceInternal(reference)
                .dto();
    }

    @Override
    public Optional<PaymentLogDto> findByPayableTypeAndPayableId(PayableType payableType, String payableId) {
        return paymentLogRepository.findByPayableTypeAndPayableIdAndRecordStatus(payableType, payableId, RecordStatus.ACTIVE)
                .map(PaymentLog::dto);
    }

}
