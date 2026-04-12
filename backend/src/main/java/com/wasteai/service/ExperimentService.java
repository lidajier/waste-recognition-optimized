package com.wasteai.service;

import com.wasteai.domain.ExperimentRunEntity;
import com.wasteai.domain.ImageEntity;
import com.wasteai.domain.UserEntity;
import com.wasteai.dto.ExperimentRunDto;
import com.wasteai.dto.ExperimentRunRequest;
import com.wasteai.dto.ExperimentRunsResponse;
import com.wasteai.repository.ExperimentRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExperimentService {

    private final ExperimentRunRepository experimentRunRepository;
    private final AuthService authService;
    private final FileStorageService fileStorageService;

    public ExperimentService(ExperimentRunRepository experimentRunRepository, AuthService authService, FileStorageService fileStorageService) {
        this.experimentRunRepository = experimentRunRepository;
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ExperimentRunDto create(ExperimentRunRequest request) {
        UserEntity user = authService.requireCurrentUser();
        ExperimentRunEntity entity = new ExperimentRunEntity();
        entity.setUser(user);
        entity.setImage(resolveImage(request.imageId()));
        entity.setModelVersion(blankToDefault(request.modelVersion(), "unknown"));
        entity.setImgsz(request.imgsz());
        entity.setConf(request.conf());
        entity.setIou(request.iou());
        entity.setLatencyMs(request.latencyMs());
        entity.setBoxCount(request.boxCount());
        entity.setAvgConfidence(request.avgConfidence());
        entity.setTopClass(blankToNull(request.topClass()));
        entity.setMinConfidenceFilter(request.minConfidenceFilter() == null ? 0f : request.minConfidenceFilter());
        entity.setClassKeywordFilter(blankToNull(request.classKeywordFilter()));
        entity.setNote(blankToNull(request.note()));
        entity.setCreatedAt(LocalDateTime.now());
        return toDto(experimentRunRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ExperimentRunsResponse list() {
        UserEntity user = authService.requireCurrentUser();
        List<ExperimentRunDto> items = experimentRunRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .limit(50)
                .map(this::toDto)
                .toList();
        return new ExperimentRunsResponse(items);
    }

    @Transactional
    public void delete(String idRaw) {
        UUID id = parseUuid(idRaw);
        UserEntity user = authService.requireCurrentUser();
        ExperimentRunEntity entity = experimentRunRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Experiment not found: " + idRaw));
        experimentRunRepository.delete(entity);
    }

    private ExperimentRunDto toDto(ExperimentRunEntity entity) {
        ImageEntity image = entity.getImage();
        return new ExperimentRunDto(
                entity.getId().toString(),
                image == null ? null : image.getId().toString(),
                image == null ? null : "/api/files/" + image.getId() + "/content",
                entity.getModelVersion(),
                entity.getImgsz(),
                entity.getConf(),
                entity.getIou(),
                entity.getLatencyMs(),
                entity.getBoxCount(),
                entity.getAvgConfidence(),
                entity.getTopClass(),
                entity.getMinConfidenceFilter(),
                entity.getClassKeywordFilter(),
                entity.getNote(),
                entity.getCreatedAt()
        );
    }

    private ImageEntity resolveImage(String imageId) {
        if (imageId == null || imageId.isBlank()) {
            return null;
        }
        return fileStorageService.getOwnedImage(imageId.trim());
    }

    private UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid experiment id.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToDefault(String value, String fallback) {
        String normalized = blankToNull(value);
        return normalized == null ? fallback : normalized;
    }
}
