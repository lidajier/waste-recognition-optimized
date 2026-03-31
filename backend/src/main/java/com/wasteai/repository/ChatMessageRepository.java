package com.wasteai.repository;

import com.wasteai.domain.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {
    List<ChatMessageEntity> findBySession_IdOrderByCreatedAtAsc(UUID sessionId);
    Optional<ChatMessageEntity> findFirstBySession_IdAndRoleOrderByCreatedAtAsc(UUID sessionId, String role);
}
