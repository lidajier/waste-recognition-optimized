package com.wasteai.repository;

import com.wasteai.domain.DetectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DetectionRepository extends JpaRepository<DetectionEntity, UUID> {
    Optional<DetectionEntity> findFirstByImage_IdOrderByCreatedAtDesc(UUID imageId);

    Optional<DetectionEntity> findByIdAndImage_UploadedBy_Id(UUID detectionId, UUID userId);
}
