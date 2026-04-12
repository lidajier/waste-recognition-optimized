package com.wasteai.service;

import com.wasteai.domain.DetectionEntity;
import com.wasteai.domain.DetectionItemEntity;
import com.wasteai.domain.ImageEntity;
import com.wasteai.domain.UserEntity;
import com.wasteai.dto.OverviewStatsResponse;
import com.wasteai.repository.DetectionRepository;
import com.wasteai.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OverviewStatsService {

    private final AuthService authService;
    private final ImageRepository imageRepository;
    private final DetectionRepository detectionRepository;

    public OverviewStatsService(AuthService authService, ImageRepository imageRepository, DetectionRepository detectionRepository) {
        this.authService = authService;
        this.imageRepository = imageRepository;
        this.detectionRepository = detectionRepository;
    }

    @Transactional(readOnly = true)
    public OverviewStatsResponse getOverview() {
        UserEntity user = authService.requireCurrentUser();
        List<ImageEntity> images = imageRepository.findByUploadedBy_IdAndDeletedFalseOrderByUploadedAtDesc(user.getId());
        List<DetectionEntity> detections = images.stream()
                .map(image -> detectionRepository.findFirstByImage_IdOrderByCreatedAtDesc(image.getId()).orElse(null))
                .filter(detection -> detection != null)
                .toList();

        Map<String, Long> classCounts = detections.stream()
                .flatMap(detection -> detection.getItems().stream())
                .collect(Collectors.groupingBy(DetectionItemEntity::getClassName, Collectors.counting()));

        String topClass = classCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("暂无数据");

        List<OverviewStatsResponse.ClassCountDto> topClasses = classCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> new OverviewStatsResponse.ClassCountDto(entry.getKey(), entry.getValue().intValue()))
                .toList();

        Map<String, DetectionEntity> latestByImage = detections.stream()
                .collect(Collectors.toMap(d -> d.getImage().getId().toString(), Function.identity()));

        List<OverviewStatsResponse.RecentActivityDto> recentActivities = images.stream()
                .limit(6)
                .map(image -> {
                    DetectionEntity detection = latestByImage.get(image.getId().toString());
                    String imageTopClass = detection == null ? null : detection.getItems().stream()
                            .max(Comparator.comparing(DetectionItemEntity::getConfidence))
                            .map(DetectionItemEntity::getClassName)
                            .orElse(null);
                    return new OverviewStatsResponse.RecentActivityDto(
                            image.getId().toString(),
                            image.getOriginalName(),
                            imageTopClass,
                            detection == null ? null : detection.getLatencyMs(),
                            image.getUploadedAt().toString()
                    );
                })
                .toList();

        long avgLatency = detections.isEmpty() ? 0L : Math.round(detections.stream()
                .mapToLong(DetectionEntity::getLatencyMs)
                .average()
                .orElse(0));

        int totalObjects = detections.stream().mapToInt(d -> d.getItems().size()).sum();
        int flaggedCount = (int) images.stream().filter(image -> Boolean.TRUE.equals(image.getFlagged())).count();
        int favoriteCount = (int) images.stream().filter(image -> Boolean.TRUE.equals(image.getFavorite())).count();

        return new OverviewStatsResponse(
                images.size(),
                detections.size(),
                totalObjects,
                flaggedCount,
                favoriteCount,
                avgLatency,
                topClass,
                topClasses,
                recentActivities
        );
    }
}
