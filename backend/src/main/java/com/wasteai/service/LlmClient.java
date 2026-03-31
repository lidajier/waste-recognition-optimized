package com.wasteai.service;

import com.wasteai.config.AppProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

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

        String url = llmProperties.getBaseUrl() + "/chat/completions";
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
            Object choicesObj = response.get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                return fallback(messages);
            }
            Object first = choices.get(0);
            if (!(first instanceof Map<?, ?> choiceMap)) {
                return fallback(messages);
            }
            Object messageObj = choiceMap.get("message");
            if (!(messageObj instanceof Map<?, ?> messageMap)) {
                return fallback(messages);
            }
            Object contentObj = messageMap.get("content");
            return contentObj == null ? fallback(messages) : String.valueOf(contentObj);
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
}
