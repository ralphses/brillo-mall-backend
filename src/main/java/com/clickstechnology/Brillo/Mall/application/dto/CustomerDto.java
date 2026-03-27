package com.clickstechnology.Brillo.Mall.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private String id;
    private String address;
    private String customerName;
    private String customerPhoneNumber;
    private String customerEmail;
}
