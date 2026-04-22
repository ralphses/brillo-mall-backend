package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookAServiceRequest {
    private String serviceRequestId;
    private String serviceId;
    private String location;
    private boolean isHuman;
    private CustomerDto customer;
    private BigDecimal totalPrice;
    private LocalDateTime scheduledDate;
}
