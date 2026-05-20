package com.clickstechnology.Brillo.Mall.application.dto.business;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class BusinessServiceRequestDto {

    private CustomerDto customer;

    private UserDto user;

    private BusinessDto business;
    private String id;
    private String whatsappConversationId;

    private BusinessServiceDto businessService;

    private BigDecimal initialPrice;

    private BigDecimal lastOfferedPrice;

    private BigDecimal agreedPrice;

    private Integer negotiationAttempts;

    private boolean humanTakeover;

    private String notes;

    private EntityStatus status;
}
