package com.wasteai.service;

import com.wasteai.config.AppProperties;
import com.wasteai.dto.ModelUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ModelService {

    private final Path modelRoot;
    private final InferenceClient inferenceClient;

    public ModelService(AppProperties properties, InferenceClient inferenceClient) throws IOException {
        this.modelRoot = Path.of(properties.getStorage().getRootDir(), "models").toAbsolutePath().normalize();
        this.inferenceClient = inferenceClient;
        Files.createDirectories(modelRoot);
    }

    public ModelUploadResponse uploadAndUse(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded model is empty.");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "model.pt" : file.getOriginalFilename());
        String lowerName = originalName.toLowerCase();
        if (!lowerName.endsWith(".pt") && !lowerName.endsWith(".onnx")) {
            throw new IllegalArgumentException("Only .pt or .onnx model files are supported.");
        }

        Path target = modelRoot.resolve(UUID.randomUUID() + "_" + originalName);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            InferenceClient.ModelInfo info = inferenceClient.uploadModel(target);
            return new ModelUploadResponse(info.modelPath, info.modelVersion);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save uploaded model.", e);
        }
    }

    public ModelUploadResponse getCurrentModel() {
        InferenceClient.ModelInfo info = inferenceClient.getCurrentModel();
        return new ModelUploadResponse(info.modelPath, info.modelVersion);
    }
}
