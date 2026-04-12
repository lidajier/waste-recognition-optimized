package com.wasteai.dto;

import java.util.List;

public record OverviewStatsResponse(
        int totalImages,
        int totalDetections,
        int totalDetectedObjects,
        int flaggedCount,
        int favoriteCount,
        long avgLatencyMs,
        String topClass,
        List<ClassCountDto> topClasses,
        List<RecentActivityDto> recentActivities
) {
    public record ClassCountDto(String className, int count) {
    }

    public record RecentActivityDto(String imageId, String imageName, String topClass, Long latencyMs, String uploadedAt) {
    }
}
