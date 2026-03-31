package com.wasteai.service;

import com.wasteai.domain.ChatMessageEntity;
import com.wasteai.domain.ChatSessionEntity;
import com.wasteai.domain.DetectionEntity;
import com.wasteai.domain.DetectionItemEntity;
import com.wasteai.domain.ImageEntity;
import com.wasteai.domain.UserEntity;
import com.wasteai.dto.HistoryItemDto;
import com.wasteai.dto.HistoryResponse;
import com.wasteai.repository.ChatMessageRepository;
import com.wasteai.repository.ChatSessionRepository;
import com.wasteai.repository.DetectionRepository;
import com.wasteai.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class HistoryService {

    private final AuthService authService;
    private final ImageRepository imageRepository;
    private final DetectionRepository detectionRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public HistoryService(
            AuthService authService,
            ImageRepository imageRepository,
            DetectionRepository detectionRepository,
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository
    ) {
        this.authService = authService;
        this.imageRepository = imageRepository;
        this.detectionRepository = detectionRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(readOnly = true)
    public HistoryResponse getRecentHistory(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        UserEntity user = authService.requireCurrentUser();
        List<HistoryItemDto> items = imageRepository
                .findByUploadedBy_IdAndDeletedFalseOrderByUploadedAtDesc(user.getId())
                .stream()
                .limit(safeLimit)
                .map(this::toItem)
                .toList();
        return new HistoryResponse(items);
    }

    private HistoryItemDto toItem(ImageEntity image) {
        DetectionEntity detection = detectionRepository.findFirstByImage_IdOrderByCreatedAtDesc(image.getId()).orElse(null);
        ChatSessionEntity session = chatSessionRepository.findFirstByImage_IdOrderByCreatedAtDesc(image.getId()).orElse(null);
        DetectionItemEntity topItem = detection == null ? null : detection.getItems().stream()
                .max(Comparator.comparing(DetectionItemEntity::getConfidence))
                .orElse(null);

        String preview = session == null ? "" : chatMessageRepository
                .findFirstBySession_IdAndRoleOrderByCreatedAtAsc(session.getId(), "assistant")
                .map(ChatMessageEntity::getContent)
                .map(this::shorten)
                .orElse("");

        return new HistoryItemDto(
                session == null ? null : session.getId().toString(),
                image.getId().toString(),
                image.getOriginalName(),
                "/api/files/" + image.getId() + "/content",
                image.getFavorite(),
                image.getFlagged(),
                detection == null ? null : detection.getId().toString(),
                detection == null ? null : detection.getModelVersion(),
                detection == null ? null : detection.getLatencyMs(),
                detection == null ? 0 : detection.getItems().size(),
                topItem == null ? null : topItem.getClassName(),
                topItem == null ? null : topItem.getConfidence(),
                preview,
                image.getUploadedAt()
        );
    }

    private String shorten(String value) {
        String normalized = value.replace('\n', ' ').trim();
        if (normalized.length() <= 120) {
            return normalized;
        }
        return normalized.substring(0, 117) + "...";
    }
}
