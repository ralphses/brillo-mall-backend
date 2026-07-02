package com.clickstechnology.Brillo.Mall.domain.notification;

import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Optional<NotificationLog> findByCorrelationIdAndRecipientAndMessageMediumAndRecordStatus(
            String correlationId,
            String recipient,
            MessageMedium messageMedium,
            RecordStatus recordStatus);
}
