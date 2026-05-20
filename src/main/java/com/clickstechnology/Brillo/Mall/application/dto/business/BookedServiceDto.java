package com.clickstechnology.Brillo.Mall.application.dto.business;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
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
public class BookedServiceDto {
    private String id;
    private BusinessDto business;
    private UserDto user;
    private CustomerDto customer;
    private BusinessServiceRequestDto businessServiceRequest;
    private BigDecimal agreedPrice;
    private LocalDateTime scheduledDate;
    private EntityStatus status;
}
