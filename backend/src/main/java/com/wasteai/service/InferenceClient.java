package com.wasteai.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wasteai.config.AppProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InferenceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String inferUrl;
    private final String modelInfoUrl;
    private final String modelUseUrl;

    public InferenceClient(AppProperties properties) {
        this.inferUrl = properties.getInference().getBaseUrl() + "/infer";
        this.modelInfoUrl = properties.getInference().getBaseUrl() + "/model";
        this.modelUseUrl = properties.getInference().getBaseUrl() + "/model/use";
    }

    public InferenceResponse infer(Path imagePath, int imgsz, float conf, float iou) {
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            ByteArrayResource resource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return imagePath.getFileName().toString();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("imgsz", String.valueOf(imgsz));
            body.add("conf", String.valueOf(conf));
            body.add("iou", String.valueOf(iou));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<InferenceResponse> response = restTemplate.postForEntity(inferUrl, requestEntity, InferenceResponse.class);
            InferenceResponse payload = response.getBody();
            if (payload == null) {
                throw new IllegalStateException("Inference service returned empty response.");
            }
            if (payload.detections == null) {
                payload.detections = new ArrayList<>();
            }
            return payload;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read image for inference.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Inference call failed: " + e.getMessage(), e);
        }
    }

    public ModelInfo useModel(Path modelPath) {
        Map<String, String> payload = new HashMap<>();
        payload.put("model_path", modelPath.toAbsolutePath().normalize().toString());
        ResponseEntity<ModelInfo> response = restTemplate.postForEntity(modelUseUrl, payload, ModelInfo.class);
        if (response.getBody() == null) {
            throw new IllegalStateException("Model switch returned empty response.");
        }
        return response.getBody();
    }

    public ModelInfo getCurrentModel() {
        ResponseEntity<ModelInfo> response = restTemplate.getForEntity(modelInfoUrl, ModelInfo.class);
        if (response.getBody() == null) {
            throw new IllegalStateException("Model info returned empty response.");
        }
        return response.getBody();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InferenceResponse {
        @JsonProperty("model_version")
        public String modelVersion;

        @JsonProperty("latency_ms")
        public long latencyMs;

        @JsonProperty("detections")
        public List<InferenceDetection> detections;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InferenceDetection {
        @JsonProperty("class_name")
        public String className;

        @JsonProperty("confidence")
        public float confidence;

        @JsonProperty("x1")
        public float x1;

        @JsonProperty("y1")
        public float y1;

        @JsonProperty("x2")
        public float x2;

        @JsonProperty("y2")
        public float y2;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelInfo {
        @JsonProperty("model_path")
        public String modelPath;

        @JsonProperty("model_version")
        public String modelVersion;
    }
}
