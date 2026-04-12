package com.wasteai.repository;

import com.wasteai.domain.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
    List<ImageEntity> findByUploadedBy_IdAndDeletedFalseOrderByUploadedAtDesc(UUID userId);

    List<ImageEntity> findByUploadedBy_IdAndDeletedFalseAndFlaggedTrueOrderByUploadedAtDesc(UUID userId);

    Optional<ImageEntity> findByIdAndUploadedBy_IdAndDeletedFalse(UUID imageId, UUID userId);
}
