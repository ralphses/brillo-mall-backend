package com.clickstechnology.Brillo.Mall.application.dto.payments;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentLogDto {
    private String id;
    private String businessId;
    private String userId;
    private String paymentReference;
    private String email;
    private BigDecimal amount;
    private EntityStatus status;
    private PayableType payableType;
    private String payableId;
}
