package com.clickstechnology.Brillo.Mall.application.dto.payments;

import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentInitializationRequest {

    @NotNull(message = "Payable type cannot be null")
    private PayableType payableType;

    @NotBlank(message = "Payable ID cannot be blank")
    private String payableId;

    private boolean isBusiness;
}
