package com.clickstechnology.Brillo.Mall.application.dto.payments;

import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentLogDto {
    private String id;
    private String businessId;
    private String userId;
    private String paymentReference;
    private String email;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String authorizationUrl;
    private String accessCode;
    private Instant verifiedAt;
    private Instant reconciledAt;
    private String gatewayMessage;
    private PayableType payableType;
    private String payableId;
}
