package com.wasteai.controller;

import com.wasteai.dto.GalleryResponse;
import com.wasteai.service.GalleryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping("/gallery")
    public ResponseEntity<GalleryResponse> gallery(
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) Boolean flagged,
            @RequestParam(required = false) String reviewType,
            @RequestParam(required = false) Float minConfidence
    ) {
        return ResponseEntity.ok(galleryService.getGallery(limit, keyword, className, favorite, flagged, reviewType, minConfidence));
    }
}
