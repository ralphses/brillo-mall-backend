package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UpdateBookingRequest {
    private BigDecimal agreedPrice;
    private LocalDateTime scheduledDate;
    private EntityStatus status;
    private CustomerDto customer;
}
