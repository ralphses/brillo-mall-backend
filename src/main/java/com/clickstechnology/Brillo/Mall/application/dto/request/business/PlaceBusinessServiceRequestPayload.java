package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.enums.ServiceRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceBusinessServiceRequestPayload {

    private String businessServiceId;
    private BigDecimal lastOfferedPrice;
    private BigDecimal agreedPrice;
    private Boolean humanTakeover;
    private String notes;
    private String whatsappConversationId;
    private ServiceRequestStatus requestStatus;

    private CustomerDto customer;

}
