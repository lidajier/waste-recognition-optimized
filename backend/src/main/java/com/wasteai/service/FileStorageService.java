package com.wasteai.service;

import com.wasteai.config.AppProperties;
import com.wasteai.domain.ImageEntity;
import com.wasteai.domain.UserEntity;
import com.wasteai.dto.ImageUpdateRequest;
import com.wasteai.repository.ImageRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootDir;
    private final ImageRepository imageRepository;
    private final AuthService authService;

    public FileStorageService(AppProperties properties, ImageRepository imageRepository, AuthService authService) throws IOException {
        this.rootDir = Path.of(properties.getStorage().getRootDir()).toAbsolutePath().normalize();
        this.imageRepository = imageRepository;
        this.authService = authService;
        Files.createDirectories(this.rootDir);
    }

    public ImageEntity store(MultipartFile file) {
        UserEntity currentUser = authService.requireCurrentUser();
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "unknown.jpg" : file.getOriginalFilename());
        String extension = "";
        int idx = originalName.lastIndexOf('.');
        if (idx >= 0) {
            extension = originalName.substring(idx);
        }

        String storedName = UUID.randomUUID() + extension;
        Path target = rootDir.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save uploaded file.", e);
        }

        ImageEntity entity = new ImageEntity();
        entity.setOriginalName(originalName);
        entity.setStoredPath(target.toString());
        entity.setUploadedBy(currentUser);
        entity.setUploadedAt(LocalDateTime.now());
        return imageRepository.save(entity);
    }

    public ImageEntity getOwnedImage(String imageIdRaw) {
        UserEntity currentUser = authService.requireCurrentUser();
        java.util.UUID imageId;
        try {
            imageId = java.util.UUID.fromString(imageIdRaw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid imageId.");
        }
        return imageRepository.findByIdAndUploadedBy_IdAndDeletedFalse(imageId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageIdRaw));
    }

    public Resource loadAsResource(String imageIdRaw) {
        return new FileSystemResource(getOwnedImage(imageIdRaw).getStoredPath());
    }

    public ImageEntity updateImage(String imageIdRaw, ImageUpdateRequest request) {
        ImageEntity image = getOwnedImage(imageIdRaw);
        if (request.favorite() != null) {
            image.setFavorite(request.favorite());
        }
        if (request.flagged() != null) {
            image.setFlagged(request.flagged());
        }
        if (request.reviewNote() != null) {
            image.setReviewNote(request.reviewNote().isBlank() ? null : request.reviewNote().trim());
        }
        if (request.reviewType() != null) {
            image.setReviewType(request.reviewType().isBlank() ? null : request.reviewType().trim());
        }
        if (request.correctedClass() != null) {
            image.setCorrectedClass(request.correctedClass().isBlank() ? null : request.correctedClass().trim());
        }
        return imageRepository.save(image);
    }

    public void softDeleteImage(String imageIdRaw) {
        ImageEntity image = getOwnedImage(imageIdRaw);
        image.setDeleted(true);
        imageRepository.save(image);
    }
}
