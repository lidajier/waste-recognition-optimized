package com.wasteai.repository;

import com.wasteai.domain.ChatSessionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, UUID> {
    List<ChatSessionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<ChatSessionEntity> findByIdAndImage_UploadedBy_Id(UUID id, UUID userId);

    Optional<ChatSessionEntity> findFirstByImage_IdOrderByCreatedAtDesc(UUID imageId);
}
