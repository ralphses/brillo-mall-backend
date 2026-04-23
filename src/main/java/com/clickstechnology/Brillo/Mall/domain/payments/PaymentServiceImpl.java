package com.clickstechnology.Brillo.Mall.domain.payments;

import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentService;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentLogDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
                .build();
        paymentLogRepository.save(paymentLog);
    }

    @Transactional
    @Override
    public void updateStatus(String paymentLogId, EntityStatus status) {
        PaymentLog existingPaymentLog = findByReference(paymentLogId);

        existingPaymentLog.setStatus(status);
        paymentLogRepository.save(existingPaymentLog);
    }

    @Override
    public void updateStatusWithPaymentReference(String reference, EntityStatus status) {
        PaymentLog paymentLog = findByPaymentReferenceInternal(reference);

        PayableType payableType = paymentLog.getPayableType();
        if (payableType == PayableType.BOOKING) {

        }

        paymentLog.setStatus(status);
        paymentLogRepository.save(paymentLog);
    }

    private PaymentLog findByPaymentReferenceInternal(String reference) {
        return paymentLogRepository.findByPaymentReference(reference)
                .orElseThrow(() -> new BusinessException("Payment log not found"));
    }

    private PaymentLog findByReference(String paymentLogId) {
        return paymentLogRepository.findByReference(paymentLogId)
                .orElseThrow(() -> new BusinessException("Payment log not found"));
    }

    @Override
    public PaymentLogDto findByPaymentReference(String reference) {
        return findByReference(reference)
                .dto();
    }

}
