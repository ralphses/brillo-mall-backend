package com.clickstechnology.Brillo.Mall.domain.payments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    Optional<PaymentLog> findByReference(String reference);
    Optional<PaymentLog> findByPaymentReference(String reference);

}
