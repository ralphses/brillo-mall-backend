package com.clickstechnology.Brillo.Mall.domain.log;

import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
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

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "brillo_all_request",
        indexes = {
                @Index(name = "idx_request_reference", columnList = "reference"),
                @Index(name = "idx_request_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_request_reference", columnNames = "reference")
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
class AllRequest extends JpaAuditor implements Serializable {

    @Column(name = "request_body", nullable = false, columnDefinition = "LONGTEXT")
    private String requestBody;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RequestStatus status;

    @Column(name = "response_body", columnDefinition = "LONGTEXT")
    private String responseBody;

    @Column(name = "origin", length = 100)
    private String origin;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

}
