package com.clickstechnology.Brillo.Mall.application.features.runtime;

import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
class OpenAiChatGptAiClient implements ChatGptAiClient {

    private final AppPropertiesConfig appPropertiesConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;

    private final Map<String, CachedAiValue> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<AiIntentDecision> classifyIntent(String normalizedInput, Set<String> candidateIntents, Map<String, Object> context) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        String cacheKey = "intent:" + normalizedInput + ":" + candidateIntents.hashCode();
        Optional<AiIntentDecision> cached = readCached(cacheKey, AiIntentDecision.class);
        if (cached.isPresent()) {
            return cached;
        }

        String systemPrompt = """
                You are Brillo's intent classifier.
                Return JSON only with keys: intentKey, routeTo, confidence, ambiguous, candidates, reason.
                Choose exactly one known intent key from the supplied list.
                Keep confidence between 0 and 1.
                """;

        String userPrompt = buildJsonPrompt(buildPromptPayload(normalizedInput, candidateIntents, context));

        Optional<AiIntentDecision> result = callOpenAiForObject(systemPrompt, userPrompt, response -> {
            String intentKey = response.path("intentKey").asText(null);
            String routeTo = response.path("routeTo").asText(null);
            double confidence = response.path("confidence").asDouble(0.0d);
            boolean ambiguous = response.path("ambiguous").asBoolean(false);
            List<String> candidates = objectMapper.convertValue(response.path("candidates"), new TypeReference<List<String>>() {});
            String reason = response.path("reason").asText(null);
            return new AiIntentDecision(intentKey, routeTo, confidence, ambiguous, candidates, reason, appPropertiesConfig.getOpenAi().getModel());
        });

        result.ifPresent(value -> writeCached(cacheKey, value));
        return result;
    }

    @Override
    public Optional<AiAmbiguityDecision> detectAmbiguity(String normalizedInput, Set<String> candidateIntents, Map<String, Object> context) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        String cacheKey = "ambiguity:" + normalizedInput + ":" + candidateIntents.hashCode();
        Optional<AiAmbiguityDecision> cached = readCached(cacheKey, AiAmbiguityDecision.class);
        if (cached.isPresent()) {
            return cached;
        }

        String systemPrompt = """
                You detect ambiguity for Brillo routing.
                Return JSON only with keys: ambiguous, preferredIntentKey, preferredRouteTo, confidence, candidates, reason.
                Prefer a structured fallback when the input could reasonably map to more than one journey.
                """;

        String userPrompt = buildJsonPrompt(buildPromptPayload(normalizedInput, candidateIntents, context));

        Optional<AiAmbiguityDecision> result = callOpenAiForObject(systemPrompt, userPrompt, response -> {
            boolean ambiguous = response.path("ambiguous").asBoolean(false);
            String preferredIntentKey = response.path("preferredIntentKey").asText(null);
            String preferredRouteTo = response.path("preferredRouteTo").asText(null);
            double confidence = response.path("confidence").asDouble(0.0d);
            List<String> candidates = objectMapper.convertValue(response.path("candidates"), new TypeReference<List<String>>() {});
            String reason = response.path("reason").asText(null);
            return new AiAmbiguityDecision(ambiguous, preferredIntentKey, preferredRouteTo, confidence, candidates, reason, appPropertiesConfig.getOpenAi().getModel());
        });

        result.ifPresent(value -> writeCached(cacheKey, value));
        return result;
    }

    @Override
    public Optional<AiSlotDecision> extractSlots(String normalizedInput, String taskKey, String stateKey, Set<String> allowedSlots, Map<String, Object> context) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        String cacheKey = "slots:" + taskKey + ":" + stateKey + ":" + normalizedInput + ":" + allowedSlots.hashCode();
        Optional<AiSlotDecision> cached = readCached(cacheKey, AiSlotDecision.class);
        if (cached.isPresent()) {
            return cached;
        }

        String systemPrompt = """
                You extract only the allowed slots for Brillo.
                Return JSON only with keys: slots, confidence, reason.
                Do not invent values. Use null when a slot cannot be determined.
                """;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("input", normalizedInput);
        payload.put("taskKey", taskKey);
        payload.put("stateKey", stateKey);
        payload.put("allowedSlots", allowedSlots);
        payload.put("context", context == null ? Map.of() : context);
        String userPrompt = buildJsonPrompt(payload);

        Optional<AiSlotDecision> result = callOpenAiForObject(systemPrompt, userPrompt, response -> {
            Map<String, Object> slots = objectMapper.convertValue(response.path("slots"), new TypeReference<LinkedHashMap<String, Object>>() {});
            double confidence = response.path("confidence").asDouble(0.0d);
            String reason = response.path("reason").asText(null);
            return new AiSlotDecision(slots, confidence, reason, appPropertiesConfig.getOpenAi().getModel());
        });

        result.ifPresent(value -> writeCached(cacheKey, value));
        return result;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(appPropertiesConfig.getOpenAi().getEnabled())
                && appPropertiesConfig.getOpenAi().getApiKey() != null
                && !appPropertiesConfig.getOpenAi().getApiKey().isBlank();
    }

    private <T> Optional<T> callOpenAiForObject(String systemPrompt, String userPrompt, Function<JsonNode, T> mapper) {
        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("model", appPropertiesConfig.getOpenAi().getModel());
            request.put("temperature", 0);
            request.put("response_format", Map.of("type", "json_object"));
            request.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));

            RestTemplate restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofSeconds(appPropertiesConfig.getOpenAi().getTimeoutSeconds()))
                    .setReadTimeout(Duration.ofSeconds(appPropertiesConfig.getOpenAi().getTimeoutSeconds()))
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(appPropertiesConfig.getOpenAi().getApiKey());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String baseUrl = appPropertiesConfig.getOpenAi().getBaseUrl();
            JsonNode response = restTemplate.postForObject(baseUrl + "/chat/completions", entity, JsonNode.class);
            if (response == null || response.path("choices").isEmpty()) {
                return Optional.empty();
            }
            String content = response.path("choices").get(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                return Optional.empty();
            }
            JsonNode parsed = objectMapper.readTree(content);
            return Optional.ofNullable(mapper.apply(parsed));
        } catch (Exception ex) {
            log.debug("OpenAI request failed, falling back to rules: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String buildJsonPrompt(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return payload.toString();
        }
    }

    private Map<String, Object> buildPromptPayload(String normalizedInput, Set<String> candidateIntents, Map<String, Object> context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("input", normalizedInput);
        payload.put("candidateIntents", candidateIntents);
        payload.put("context", context == null ? Map.of() : context);
        return payload;
    }

    private <T> Optional<T> readCached(String cacheKey, Class<T> clazz) {
        CachedAiValue cached = cache.get(cacheKey);
        if (cached == null || cached.expiresAt() < System.currentTimeMillis()) {
            cache.remove(cacheKey);
            return Optional.empty();
        }
        return Optional.ofNullable(objectMapper.convertValue(cached.value(), clazz));
    }

    private void writeCached(String cacheKey, Object value) {
        long ttlMillis = Duration.ofSeconds(appPropertiesConfig.getOpenAi().getCacheTtlSeconds()).toMillis();
        cache.put(cacheKey, new CachedAiValue(value, System.currentTimeMillis() + ttlMillis));
    }

    private record CachedAiValue(Object value, long expiresAt) {
    }
}
