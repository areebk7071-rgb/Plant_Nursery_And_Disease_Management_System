package com.plantmanager.service;

import com.plantmanager.model.AiConfig;
import com.plantmanager.model.Plant;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class AiService {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final HttpClient httpClient;
    private AiConfig config;

    public AiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.config = new AiConfig();
    }

    public AiService(AiConfig config) {
        this();
        this.config = config;
    }

    public void setConfig(AiConfig config) {
        this.config = config;
    }

    public AiConfig getConfig() {
        return config;
    }

    public String ask(String userMessage) throws Exception {
        return ask(userMessage, List.of());
    }

    public String ask(String userMessage, List<Plant> plants) throws Exception {
        String systemPrompt = buildSystemPrompt(plants);
        String body = buildRequestBody(systemPrompt, userMessage);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(config.getApiUrl()))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .method("POST", HttpRequest.BodyPublishers.ofString(body));

        if (!config.getApiKey().isBlank()) {
            builder.header("Authorization", "Bearer " + config.getApiKey());
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API returned status " + response.statusCode()
                    + ": " + response.body());
        }

        return extractContent(response.body());
    }

    private String buildSystemPrompt(List<Plant> plants) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are a botanical advisor assistant. You help users with plant care,
                disease identification, and treatment recommendations.
                Keep responses concise, practical, and science-based.
                """);

        if (!plants.isEmpty()) {
            prompt.append("\n\nThe user has the following plants in their care:\n");
            for (Plant p : plants) {
                prompt.append("- ").append(p.getName())
                        .append(" (").append(p.getSpecies()).append(")")
                        .append(" — Type: ").append(p.getPlantType())
                        .append(", Health: ").append(p.getHealthStatus());
                if (p.hasDisease()) {
                    prompt.append(", Disease: ").append(p.getAssignedDiseaseName());
                }
                prompt.append("\n");
            }
        }

        return prompt.toString();
    }

    private String buildRequestBody(String systemPrompt, String userMessage) {
        String escapedSystem = escapeJson(systemPrompt);
        String escapedUser = escapeJson(userMessage);

        return """
                {
                    "model": "%s",
                    "messages": [
                        {"role": "system", "content": "%s"},
                        {"role": "user", "content": "%s"}
                    ],
                    "temperature": 0.7,
                    "max_tokens": 1000
                }
                """.formatted(config.getModelName(), escapedSystem, escapedUser);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String extractContent(String responseBody) {
        try {
            int choicesIndex = responseBody.indexOf("\"choices\"");
            if (choicesIndex < 0) return parseError(responseBody);

            int messageIndex = responseBody.indexOf("\"message\"", choicesIndex);
            if (messageIndex < 0) return parseError(responseBody);

            int contentIndex = responseBody.indexOf("\"content\"", messageIndex);
            if (contentIndex < 0) return parseError(responseBody);

            int start = responseBody.indexOf("\"", contentIndex + 10);
            if (start < 0) return parseError(responseBody);
            start = start + 1;

            StringBuilder content = new StringBuilder();
            for (int i = start; i < responseBody.length(); i++) {
                char c = responseBody.charAt(i);
                if (c == '\\' && i + 1 < responseBody.length()) {
                    char next = responseBody.charAt(i + 1);
                    switch (next) {
                        case 'n' -> content.append('\n');
                        case 't' -> content.append('\t');
                        case '\\' -> content.append('\\');
                        case '"' -> content.append('"');
                        default -> { content.append(c); content.append(next); }
                    }
                    i++;
                } else if (c == '"') {
                    break;
                } else {
                    content.append(c);
                }
            }
            return content.toString().strip();
        } catch (Exception e) {
            return "Could not parse AI response. Raw response:\n" + responseBody;
        }
    }

    private String parseError(String body) {
        try {
            int msgIdx = body.indexOf("\"message\"");
            if (msgIdx >= 0) {
                int start = body.indexOf("\"", msgIdx + 9);
                if (start >= 0) {
                    start = start + 1;
                    int end = body.indexOf("\"", start);
                    if (end > start) return "Error: " + body.substring(start, end);
                }
            }
        } catch (Exception ignored) {}
        return "Unexpected response format. Check your API URL and model name.";
    }
}
