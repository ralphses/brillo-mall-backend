package com.clickstechnology.Brillo.Mall.application.dto.order;


import com.clickstechnology.Brillo.Mall.application.annotations.EnumValue;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Status cannot be blank")
    @EnumValue(enumClass = OrderStatus.class, message = "Invalid order status")
    private String status;
}
