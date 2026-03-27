package com.clickstechnology.Brillo.Mall.application.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderPlacedResponse {

    private final String message;
    private final OrderDto order;
}
