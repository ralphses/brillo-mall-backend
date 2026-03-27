package com.clickstechnology.Brillo.Mall.domain.customer;


import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BRILLO_CUSTOMER")
class Customer extends JpaAuditor implements Serializable {
    private String name;
    private String email;
    private String phone;
    private String address;

    public CustomerDto dto() {
        return CustomerDto.builder()
                .id(reference)
                .customerName(name)
                .customerEmail(email)
                .customerPhoneNumber(phone)
                .address(address)
                .build();
    }
}
