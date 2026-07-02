package com.clickstechnology.Brillo.Mall.application.features.runtime;

import java.util.Map;

public record AiSlotDecision(
        Map<String, Object> slots,
        double confidence,
        String reason,
        String model
) {
}
