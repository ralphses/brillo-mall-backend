package com.clickstechnology.Brillo.Mall.application.dto;

import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorefrontData {
    private BusinessCategory category;
    private String id;
    private String email;
    private String name;
    private String phoneNumber;
    private String address;
    private String whatsappNumber;
    private WhatsappType whatsappType;
    private Boolean setUpCompleted;
    private Boolean active;
    private String description;
    private String logoUrl;

    private List<StoreOrderDto> orders;
    private List<StoreTransactionDto> transactions;
    private List<StoreTeamMemberDto> members;
    private List<StoreCustomerDto> customers;
}
