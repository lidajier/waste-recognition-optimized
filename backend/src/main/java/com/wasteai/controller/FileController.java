package com.wasteai.controller;

import com.wasteai.domain.ImageEntity;
import com.wasteai.dto.GalleryItemDto;
import com.wasteai.dto.ImageUpdateRequest;
import com.wasteai.dto.UploadResponse;
import com.wasteai.service.FileStorageService;
import com.wasteai.service.GalleryService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final GalleryService galleryService;

    public FileController(FileStorageService fileStorageService, GalleryService galleryService) {
        this.fileStorageService = fileStorageService;
        this.galleryService = galleryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        ImageEntity image = fileStorageService.store(file);
        return ResponseEntity.ok(new UploadResponse(image.getId().toString(), image.getOriginalName(), "/api/files/" + image.getId() + "/content"));
    }

    @GetMapping("/{imageId}/content")
    public ResponseEntity<Resource> content(@PathVariable String imageId) throws Exception {
        ImageEntity image = fileStorageService.getOwnedImage(imageId);
        Resource resource = fileStorageService.loadAsResource(imageId);
        String mediaType = Files.probeContentType(Path.of(image.getStoredPath()));
        return ResponseEntity.ok()
                .contentType(mediaType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(mediaType))
                .body(resource);
    }

    @PatchMapping("/{imageId}")
    public ResponseEntity<GalleryItemDto> update(@PathVariable String imageId, @RequestBody ImageUpdateRequest request) {
        ImageEntity image = fileStorageService.updateImage(imageId, request);
        GalleryItemDto dto = galleryService.getGallery(200).items().stream()
                .filter(item -> item.imageId().equals(image.getId().toString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable String imageId) {
        fileStorageService.softDeleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
