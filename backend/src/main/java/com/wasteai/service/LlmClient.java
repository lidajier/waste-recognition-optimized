package com.wasteai.service;

import com.wasteai.config.AppProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class LlmClient {

    private static final HttpClient STREAMING_CLIENT = HttpClient.newHttpClient();

    public record LlmMessage(String role, String content) {
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private final AppProperties.Llm llmProperties;

    public LlmClient(AppProperties properties) {
        this.llmProperties = properties.getLlm();
    }

    public String chat(List<LlmMessage> messages) {
        if (llmProperties.getApiKey() == null || llmProperties.getApiKey().isBlank()) {
            return fallback(messages);
        }

        String url = appendPath(llmProperties.getBaseUrl(), "/chat/completions");
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", llmProperties.getModel());
        payload.put("temperature", llmProperties.getTemperature());
        payload.put("messages", messages.stream().map(m -> {
            Map<String, String> row = new HashMap<>();
            row.put("role", m.role());
            row.put("content", m.content());
            return row;
        }).toList());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(llmProperties.getApiKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            if (response == null) {
                return fallback(messages);
            }
            String content = extractMessageContent(response);
            if ((content == null || content.isBlank()) && supportsStreamingFallback()) {
                content = streamChat(messages);
            }
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("LLM proxy returned an empty assistant message. "
                        + "The endpoint is reachable, but no readable text was returned for model `"
                        + llmProperties.getModel()
                        + "`. Please verify the proxy's OpenAI-compatible response format.");
            }
            return content;
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("LLM request failed: " + e.getStatusCode().value() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new IllegalStateException("LLM request failed: " + e.getMessage(), e);
        }
    }

    private String fallback(List<LlmMessage> messages) {
        List<String> lastUserMessages = new ArrayList<>();
        for (LlmMessage message : messages) {
            if ("user".equals(message.role())) {
                lastUserMessages.add(message.content());
            }
        }
        String last = lastUserMessages.isEmpty() ? "No user message found." : lastUserMessages.get(lastUserMessages.size() - 1);
        return "当前本地大模型未正确配置，暂时无法生成正式回答。\n"
                + "你刚才的问题是：\n" + last + "\n\n"
                + "请检查本地模型服务是否启动，以及 `LLM_BASE_URL`、`LLM_API_KEY`、`LLM_MODEL` 是否正确。";
    }

    private String appendPath(String baseUrl, String path) {
        String normalizedBase = Objects.requireNonNullElse(baseUrl, "").trim();
        if (normalizedBase.endsWith("/")) {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }
        return normalizedBase + path;
    }

    private boolean supportsStreamingFallback() {
        return llmProperties.getBaseUrl() != null && !llmProperties.getBaseUrl().isBlank();
    }

    private String streamChat(List<LlmMessage> messages) throws Exception {
        String url = appendPath(llmProperties.getBaseUrl(), "/chat/completions");
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", llmProperties.getModel());
        payload.put("temperature", llmProperties.getTemperature());
        payload.put("stream", true);
        payload.put("messages", messages.stream().map(m -> {
            Map<String, String> row = new HashMap<>();
            row.put("role", m.role());
            row.put("content", m.content());
            return row;
        }).toList());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + llmProperties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(toJson(payload), StandardCharsets.UTF_8))
                .build();

        HttpResponse<java.io.InputStream> response = STREAMING_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new IllegalStateException("LLM streaming request failed: " + response.statusCode() + " " + errorBody);
        }

        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) {
                    continue;
                }
                String data = line.substring(6).trim();
                if (data.isEmpty() || "[DONE]".equals(data)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> chunk = restTemplate.getMessageConverters().stream()
                        .filter(converter -> converter instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter)
                        .map(converter -> (org.springframework.http.converter.json.MappingJackson2HttpMessageConverter) converter)
                        .findFirst()
                        .orElseThrow()
                        .getObjectMapper()
                        .readValue(data, Map.class);
                Object choicesObj = chunk.get("choices");
                if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                    continue;
                }
                Object first = choices.get(0);
                if (!(first instanceof Map<?, ?> choiceMap)) {
                    continue;
                }
                Object deltaObj = choiceMap.get("delta");
                if (!(deltaObj instanceof Map<?, ?> deltaMap)) {
                    continue;
                }
                String part = normalizeContent(deltaMap.get("content"));
                if (part != null && !part.isBlank()) {
                    text.append(part);
                }
            }
        }
        return text.toString();
    }

    private String toJson(Map<String, Object> payload) throws Exception {
        return restTemplate.getMessageConverters().stream()
                .filter(converter -> converter instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter)
                .map(converter -> (org.springframework.http.converter.json.MappingJackson2HttpMessageConverter) converter)
                .findFirst()
                .orElseThrow()
                .getObjectMapper()
                .writeValueAsString(payload);
    }

    private String extractMessageContent(Map<String, Object> response) {
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> choiceMap)) {
            return null;
        }
        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            return null;
        }
        return normalizeContent(messageMap.get("content"));
    }

    private String normalizeContent(Object contentObj) {
        if (contentObj == null) {
            return null;
        }
        if (contentObj instanceof String content) {
            return content;
        }
        if (contentObj instanceof List<?> contentList) {
            return contentList.stream()
                    .map(this::extractContentPart)
                    .filter(part -> part != null && !part.isBlank())
                    .collect(Collectors.joining("\n"));
        }
        if (contentObj.getClass().isArray()) {
            return Arrays.stream((Object[]) contentObj)
                    .map(this::extractContentPart)
                    .filter(part -> part != null && !part.isBlank())
                    .collect(Collectors.joining("\n"));
        }
        return String.valueOf(contentObj);
    }

    private String extractContentPart(Object partObj) {
        if (partObj == null) {
            return null;
        }
        if (partObj instanceof String part) {
            return part;
        }
        if (partObj instanceof Map<?, ?> partMap) {
            Object text = partMap.get("text");
            if (text != null) {
                return String.valueOf(text);
            }
            Object nested = partMap.get("content");
            if (nested != null) {
                return normalizeContent(nested);
            }
        }
        return String.valueOf(partObj);
    }
}
