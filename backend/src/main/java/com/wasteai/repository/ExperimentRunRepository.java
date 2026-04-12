package com.wasteai.repository;

import com.wasteai.domain.ExperimentRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExperimentRunRepository extends JpaRepository<ExperimentRunEntity, UUID> {
    List<ExperimentRunEntity> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}
