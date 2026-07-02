package com.clickstechnology.Brillo.Mall.domain.payments;

import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    Optional<PaymentLog> findByReferenceAndRecordStatus(String reference, RecordStatus recordStatus);

    Optional<PaymentLog> findByPaymentReferenceAndRecordStatus(String reference, RecordStatus recordStatus);

    Optional<PaymentLog> findByPayableTypeAndPayableIdAndRecordStatus(
            PayableType payableType,
            String payableId,
            RecordStatus recordStatus);

}
