package com.wasteai.service;

import com.wasteai.domain.DetectionEntity;
import com.wasteai.domain.DetectionItemEntity;
import com.wasteai.domain.ImageEntity;
import com.wasteai.domain.UserEntity;
import com.wasteai.dto.DetectRequest;
import com.wasteai.dto.DetectResponse;
import com.wasteai.dto.DetectionBoxDto;
import com.wasteai.repository.DetectionRepository;
import com.wasteai.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DetectionService {

    private final ImageRepository imageRepository;
    private final DetectionRepository detectionRepository;
    private final InferenceClient inferenceClient;
    private final AuthService authService;

    public DetectionService(ImageRepository imageRepository, DetectionRepository detectionRepository, InferenceClient inferenceClient, AuthService authService) {
        this.imageRepository = imageRepository;
        this.detectionRepository = detectionRepository;
        this.inferenceClient = inferenceClient;
        this.authService = authService;
    }

    @Transactional
    public DetectResponse detect(DetectRequest request) {
        UUID imageId = parseUuid(request.imageId(), "Invalid imageId.");
        UserEntity currentUser = authService.requireCurrentUser();
        ImageEntity image = imageRepository.findById(imageId)
                .filter(candidate -> candidate.getUploadedBy().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + request.imageId()));

        int imgsz = request.imgsz() == null ? 512 : request.imgsz();
        float conf = request.conf() == null ? 0.25f : request.conf();
        float iou = request.iou() == null ? 0.7f : request.iou();

        InferenceClient.InferenceResponse inference = inferenceClient.infer(Path.of(image.getStoredPath()), imgsz, conf, iou);

        DetectionEntity detection = new DetectionEntity();
        detection.setImage(image);
        detection.setModelVersion(inference.modelVersion == null ? "unknown" : inference.modelVersion);
        detection.setImgsz(imgsz);
        detection.setConf(conf);
        detection.setIou(iou);
        detection.setLatencyMs(inference.latencyMs);
        detection.setCreatedAt(LocalDateTime.now());

        for (InferenceClient.InferenceDetection det : inference.detections) {
            DetectionItemEntity item = new DetectionItemEntity();
            item.setDetection(detection);
            item.setClassName(det.className);
            item.setConfidence(det.confidence);
            item.setX1(det.x1);
            item.setY1(det.y1);
            item.setX2(det.x2);
            item.setY2(det.y2);
            detection.getItems().add(item);
        }

        DetectionEntity saved = detectionRepository.save(detection);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DetectResponse getLatestByImage(String imageIdRaw) {
        UUID imageId = parseUuid(imageIdRaw, "Invalid imageId.");
        UserEntity currentUser = authService.requireCurrentUser();
        DetectionEntity detection = detectionRepository.findFirstByImage_IdOrderByCreatedAtDesc(imageId)
                .filter(candidate -> candidate.getImage().getUploadedBy().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new IllegalArgumentException("No detection found for image: " + imageIdRaw));
        return toResponse(detection);
    }

    @Transactional(readOnly = true)
    public DetectionEntity getDetectionEntity(String detectionIdRaw) {
        UUID detectionId = parseUuid(detectionIdRaw, "Invalid detectionId.");
        UserEntity currentUser = authService.requireCurrentUser();
        return detectionRepository.findByIdAndImage_UploadedBy_Id(detectionId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Detection not found: " + detectionIdRaw));
    }

    private DetectResponse toResponse(DetectionEntity detection) {
        List<DetectionBoxDto> boxes = detection.getItems().stream()
                .map(i -> new DetectionBoxDto(
                        i.getClassName(),
                        i.getConfidence(),
                        i.getX1(),
                        i.getY1(),
                        i.getX2(),
                        i.getY2()
                ))
                .toList();

        return new DetectResponse(
                detection.getId().toString(),
                detection.getImage().getId().toString(),
                detection.getModelVersion(),
                detection.getLatencyMs(),
                boxes
        );
    }

    private UUID parseUuid(String raw, String errorMsg) {
        try {
            return UUID.fromString(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
