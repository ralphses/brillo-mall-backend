package com.clickstechnology.Brillo.Mall.application.dto;

import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDto {

    private String id;
    private String ownerId;
    private String name;
    private String slug;
    private BusinessCategory category;
    private EntityStatus status;
    private String description;
    private String email;
    private String phoneNumber;
    private String logoUrl;
    private String state;
    private String city;
    private String address;
    private WhatsappType whatsappType;
    private String whatsappNumber;
    private String storefrontName;
    private Boolean storefrontActive;
    private Boolean isActive;
    private Boolean setupCompleted;
}