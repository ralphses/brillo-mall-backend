package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentLogDto;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentService {
    void createNewLog(String businessId, String userId, String paymentReference, BigDecimal amount,PayableType payableType, String payableId, String email);

    void updateInitialization(String paymentLogId, String authorizationUrl, String accessCode, PaymentStatus status);
    void updateStatus(String paymentLogId, PaymentStatus status);
    void updateStatusWithPaymentReference(String reference, PaymentStatus status);

    PaymentLogDto findByPaymentReference(String reference);

    Optional<PaymentLogDto> findByPayableTypeAndPayableId(PayableType payableType, String payableId);

}
