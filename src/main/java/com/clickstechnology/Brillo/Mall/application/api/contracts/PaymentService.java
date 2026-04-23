package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentLogDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;

import java.math.BigDecimal;

public interface PaymentService {
    void createNewLog(String businessId, String userId, String paymentReference, BigDecimal amount,PayableType payableType, String payableId, String email);

    void updateStatus(String paymentLogId, EntityStatus status);
    void updateStatusWithPaymentReference(String reference, EntityStatus status);

    PaymentLogDto findByPaymentReference(String reference);

}
