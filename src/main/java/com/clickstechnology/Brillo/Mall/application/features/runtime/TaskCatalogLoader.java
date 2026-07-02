package com.clickstechnology.Brillo.Mall.application.features.runtime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Getter
public class TaskCatalogLoader {

    private final ObjectMapper objectMapper;

    private Map<String, JsonNode> intents = Map.of();
    private Map<String, JsonNode> tasks = Map.of();
    private Map<String, JsonNode> slots = Map.of();
    private Map<String, String> exampleToIntent = Map.of();
    private Map<String, String> intentToRoute = Map.of();

    @PostConstruct
    void load() {
        try {
            intents = readCatalog("intents.json");
            tasks = readCatalog("tasks.json");
            slots = readCatalog("slots.json");
            intentToRoute = intents.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().path("routes_to").asText(""),
                            (left, right) -> left, LinkedHashMap::new));

            Map<String, String> phrases = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> entry : intents.entrySet()) {
                for (JsonNode example : entry.getValue().path("examples")) {
                    String normalized = normalize(example.asText(""));
                    if (!normalized.isBlank()) {
                        phrases.putIfAbsent(normalized, entry.getKey());
                    }
                }
            }
            exampleToIntent = Collections.unmodifiableMap(phrases);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load intent/task/slot catalogs", e);
        }
    }

    public Set<String> knownIntentKeys() {
        return intents.keySet();
    }

    public Set<String> knownTaskKeys() {
        return tasks.keySet();
    }

    public Optional<String> routeTo(String intentKey) {
        return Optional.ofNullable(intentToRoute.get(intentKey)).filter(route -> !route.isBlank());
    }

    public List<String> intentExamples(String intentKey) {
        JsonNode node = intents.get(intentKey);
        if (node == null || !node.has("examples")) {
            return List.of();
        }
        List<String> examples = new ArrayList<>();
        for (JsonNode example : node.path("examples")) {
            examples.add(example.asText(""));
        }
        return examples;
    }

    public List<String> taskStates(String taskKey) {
        JsonNode node = tasks.get(taskKey);
        if (node == null || !node.has("states")) {
            return List.of();
        }
        List<String> states = new ArrayList<>();
        for (JsonNode state : node.path("states")) {
            states.add(state.asText(""));
        }
        return states;
    }

    public List<String> requiredSlots(String taskKey, String stateKey) {
        return stateSlots(taskKey, stateKey, "required");
    }

    public List<String> optionalSlots(String taskKey, String stateKey) {
        return stateSlots(taskKey, stateKey, "optional");
    }

    public List<String> globalSlots(String taskKey) {
        JsonNode node = slots.get(taskKey);
        if (node == null || !node.has("global_slots")) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        node.path("global_slots").fieldNames().forEachRemaining(values::add);
        return values;
    }

    public boolean isKnownIntent(String value) {
        return knownIntentKeys().contains(value);
    }

    public Optional<String> findIntentByExample(String normalizedInput) {
        return Optional.ofNullable(exampleToIntent.get(normalizedInput));
    }

    public String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s:]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public Map<String, JsonNode> intentsView() {
        return intents;
    }

    public Map<String, JsonNode> tasksView() {
        return tasks;
    }

    public Map<String, JsonNode> slotsView() {
        return slots;
    }

    private Map<String, JsonNode> readCatalog(String resourceName) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourceName);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<LinkedHashMap<String, JsonNode>>() {
            });
        }
    }

    private List<String> stateSlots(String taskKey, String stateKey, String slotType) {
        JsonNode node = slots.get(taskKey);
        if (node == null) {
            return List.of();
        }
        JsonNode stateNode = node.path("state_slots").path(stateKey);
        if (stateNode.isMissingNode() || !stateNode.has(slotType)) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode slot : stateNode.path(slotType)) {
            values.add(slot.asText(""));
        }
        return values;
    }
}
