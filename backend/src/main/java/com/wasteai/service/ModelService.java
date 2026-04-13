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
import java.util.Comparator;
import java.util.List;
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
            InferenceClient.ModelInfo info = inferenceClient.useModel(target);
            return new ModelUploadResponse(info.modelPath, info.modelVersion);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save uploaded model.", e);
        }
    }

    public ModelUploadResponse useExisting(String modelPath) {
        Path target = Path.of(modelPath).toAbsolutePath().normalize();
        if (!Files.exists(target)) {
            throw new IllegalArgumentException("Model file not found.");
        }
        InferenceClient.ModelInfo info = inferenceClient.useModel(target);
        return new ModelUploadResponse(info.modelPath, info.modelVersion);
    }

    public ModelUploadResponse getCurrentModel() {
        InferenceClient.ModelInfo info = inferenceClient.getCurrentModel();
        return new ModelUploadResponse(info.modelPath, info.modelVersion);
    }

    public List<ModelUploadResponse> listModels() {
        try {
            return Files.list(modelRoot)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".pt") || name.endsWith(".onnx");
                    })
                    .sorted(Comparator.comparing((Path path) -> path.toFile().lastModified()).reversed())
                    .map(path -> new ModelUploadResponse(path.toString(), path.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list stored models.", e);
        }
    }
}
