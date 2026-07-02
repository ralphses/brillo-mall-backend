package com.clickstechnology.Brillo.Mall.application.features.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ChatGptAiClient {
    Optional<AiIntentDecision> classifyIntent(String normalizedInput, Set<String> candidateIntents, Map<String, Object> context);

    Optional<AiAmbiguityDecision> detectAmbiguity(String normalizedInput, Set<String> candidateIntents, Map<String, Object> context);

    Optional<AiSlotDecision> extractSlots(String normalizedInput, String taskKey, String stateKey, Set<String> allowedSlots, Map<String, Object> context);

    boolean isEnabled();
}
