package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingRequest {
    private BigDecimal agreedPrice;
    private LocalDateTime scheduledDate;
    private String location;
    private BookingStatus status;
    private CustomerDto customer;
}
