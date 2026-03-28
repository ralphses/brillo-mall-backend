package com.clickstechnology.Brillo.Mall.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Customer address cannot be blank")
    private String address;

    @NotBlank(message = "Customer name cannot be blank")
    private String customerName;

    @NotBlank(message = "Customer phone number cannot be blank")
    private String customerPhoneNumber;

    @Email(message = "Invalid email format")
    private String customerEmail;
}