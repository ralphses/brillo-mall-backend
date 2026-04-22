package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlaceBusinessServiceRequestPayload {

    private String businessServiceId;
    private BigDecimal lastOfferedPrice;
    private BigDecimal initialPrice;
    private BigDecimal agreedPrice;
    private Integer negotiationAttemptsCount;
    private Boolean humanTakeover;
    private String notes;
    private String whatsappConversationId;
    private boolean isBusiness;

    private CustomerDto customer;

}
