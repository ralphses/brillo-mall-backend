package com.clickstechnology.Brillo.Mall.application.dto.request.business;

import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBusinessRequest {
    private String displayName;
    private String description;
    private BusinessLocation businessAddress;
    private String businessEmail;
    private String businessPhone;
    private String whatsappNumber;
    private WhatsappType whatsappType;

    public UpdateBusinessRequest(
            String displayName,
            String description,
            BusinessLocation businessAddress,
            String businessEmail,
            String businessPhone
    ) {
        this.displayName = displayName;
        this.description = description;
        this.businessAddress = businessAddress;
        this.businessEmail = businessEmail;
        this.businessPhone = businessPhone;
    }

    @Slf4j
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BusinessLocation {
        private String streetAddress;
        private String city;
        private String state;
    }
}
