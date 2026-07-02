package com.clickstechnology.Brillo.Mall.application.features.runtime;

import java.util.List;

public record AiIntentDecision(
        String intentKey,
        String routeTo,
        double confidence,
        boolean ambiguous,
        List<String> candidates,
        String reason,
        String model
) {
}
