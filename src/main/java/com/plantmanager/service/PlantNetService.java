package com.plantmanager.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlantNetService {

    private static final String DEFAULT_API_KEY = "";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final HttpClient httpClient;
    private String apiKey;

    public PlantNetService() {
        this(DEFAULT_API_KEY);
    }

    public PlantNetService(String apiKey) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.apiKey = apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String identify(Path imagePath) throws IOException, InterruptedException {
        return identify(List.of(imagePath));
    }

    public String identify(List<Path> imagePaths) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            return "{\"error\":\"No PlantNet API key configured. Get a free key at https://my.plantnet.org/\"}";
        }

        String boundary = "----" + UUID.randomUUID().toString();
        byte[] multipartBody = buildMultipartBody(imagePaths, boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://my-api.plantnet.org/v2/identify/all?api-key=" + apiKey))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return "{\"error\":\"PlantNet API returned status " + response.statusCode()
                    + ": " + escapeJson(response.body()) + "\"}";
        }

        return response.body();
    }

    private byte[] buildMultipartBody(List<Path> imagePaths, String boundary) throws IOException {
        byte[] boundaryBytes = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] endBytes = ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        List<byte[]> parts = new ArrayList<>();

        for (Path path : imagePaths) {
            if (!Files.exists(path)) continue;
            byte[] fileBytes = Files.readAllBytes(path);
            String fileName = path.getFileName().toString();
            String contentType = guessContentType(fileName);

            StringBuilder header = new StringBuilder();
            header.append("Content-Disposition: form-data; name=\"images\"; filename=\"")
                    .append(fileName).append("\"\r\n");
            header.append("Content-Type: ").append(contentType).append("\r\n\r\n");

            byte[] headerBytes = header.toString().getBytes(StandardCharsets.UTF_8);
            byte[] part = concat(boundaryBytes, headerBytes, fileBytes, "\r\n".getBytes(StandardCharsets.UTF_8));
            parts.add(part);
        }

        StringBuilder organHeader = new StringBuilder();
        organHeader.append("Content-Disposition: form-data; name=\"organs\"\r\n");
        organHeader.append("Content-Type: text/plain\r\n\r\n");
        organHeader.append("auto\r\n");
        byte[] organPart = concat(boundaryBytes, organHeader.toString().getBytes(StandardCharsets.UTF_8));
        parts.add(organPart);

        int totalLen = parts.stream().mapToInt(p -> p.length).sum() + endBytes.length;
        byte[] result = new byte[totalLen];
        int pos = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, pos, part.length);
            pos += part.length;
        }
        System.arraycopy(endBytes, 0, result, pos, endBytes.length);

        return result;
    }

    private String guessContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) total += a.length;
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    public static String formatPlantNetResponse(String json) {
        if (json.contains("\"error\"")) {
            int start = json.indexOf("\"error\"");
            int valStart = json.indexOf("\"", start + 8);
            if (valStart < 0) return "Error from PlantNet API.";
            valStart = valStart + 1;
            int valEnd = json.indexOf("\"", valStart);
            if (valEnd > valStart) return "Error: " + json.substring(valStart, valEnd);
            return "Error from PlantNet API.";
        }

        if (!json.contains("\"results\"")) {
            return "No results found. Try a clearer image of the plant.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("PlantNet Identification Results:\n\n");

        int resultsIdx = json.indexOf("\"results\"");
        int resultsArrayStart = json.indexOf("[", resultsIdx);
        if (resultsArrayStart < 0) return "Could not parse results.";

        int depth = 0;
        int resultsArrayEnd = -1;
        for (int i = resultsArrayStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) {
                    resultsArrayEnd = i;
                    break;
                }
            }
        }

        if (resultsArrayEnd < 0) return "Could not parse results.";

        String resultsJson = json.substring(resultsArrayStart + 1, resultsArrayEnd);
        if (resultsJson.isBlank()) {
            return "No matching species found for this plant image.";
        }

        List<String> speciesEntries = extractTopLevelObjects(resultsJson);

        int count = 0;
        for (String entry : speciesEntries) {
            if (count >= 5) break;
            String scientificName = extractJsonField(entry, "scientificName");
            if (scientificName == null) continue;

            double score = 0;
            String scoreStr = extractJsonField(entry, "score");
            if (scoreStr != null) {
                try {
                    score = Double.parseDouble(scoreStr);
                } catch (NumberFormatException ignored) {}
            }

            String commonNames = extractJsonField(entry, "commonNames");
            String gbifId = extractJsonField(entry, "gbifId");

            sb.append(count + 1).append(". ").append(scientificName);
            if (commonNames != null && !commonNames.isEmpty() && !commonNames.equals("[]")) {
                String cleanCommon = commonNames.replace("[", "").replace("]", "").replace("\"", "");
                sb.append(" (").append(cleanCommon).append(")");
            }
            sb.append(" — ").append(String.format("%.1f", score * 100)).append("% confidence");
            if (gbifId != null && !gbifId.isEmpty() && !gbifId.equals("null")) {
                sb.append(" [GBIF: ").append(gbifId).append("]");
            }
            sb.append("\n");
            count++;
        }

        if (count == 0) {
            return "No matching species found for this plant image.";
        }

        sb.append("\nYou can ask me follow-up questions about any of these plants!");
        return sb.toString();
    }

    private static String extractJsonField(String json, String field) {
        String key = "\"" + field + "\"";
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx + key.length());
        if (colonIdx < 0) return null;

        int start = colonIdx + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return null;

        if (json.charAt(start) == '"') {
            StringBuilder val = new StringBuilder();
            start++;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    val.append(json.charAt(i + 1));
                    i++;
                } else if (c == '"') {
                    break;
                } else {
                    val.append(c);
                }
            }
            return val.toString();
        } else if (json.charAt(start) == '[') {
            int depth = 0;
            StringBuilder val = new StringBuilder();
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                val.append(c);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) break;
                }
            }
            return val.toString();
        } else {
            StringBuilder val = new StringBuilder();
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == ',' || c == '}' || c == ']') break;
                val.append(c);
            }
            return val.toString().strip();
        }
    }

    private static List<String> extractTopLevelObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        boolean inObject = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    current = new StringBuilder();
                    inObject = true;
                }
                depth++;
                if (inObject) current.append(c);
            } else if (c == '}') {
                depth--;
                if (inObject) current.append(c);
                if (depth == 0 && inObject) {
                    objects.add(current.toString());
                    inObject = false;
                }
            } else if (inObject) {
                current.append(c);
            }
        }
        return objects;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
