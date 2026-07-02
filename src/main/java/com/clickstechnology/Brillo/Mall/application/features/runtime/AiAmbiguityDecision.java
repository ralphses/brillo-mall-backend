package com.clickstechnology.Brillo.Mall.application.features.runtime;

import java.util.List;

public record AiAmbiguityDecision(
        boolean ambiguous,
        String preferredIntentKey,
        String preferredRouteTo,
        double confidence,
        List<String> candidates,
        String reason,
        String model
) {
}
