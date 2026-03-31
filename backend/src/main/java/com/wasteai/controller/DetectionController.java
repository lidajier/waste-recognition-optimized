package com.wasteai.controller;

import com.wasteai.dto.DetectRequest;
import com.wasteai.dto.DetectResponse;
import com.wasteai.service.DetectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping("/detect")
    public ResponseEntity<DetectResponse> detect(@Valid @RequestBody DetectRequest request) {
        return ResponseEntity.ok(detectionService.detect(request));
    }

    @GetMapping("/results/{imageId}")
    public ResponseEntity<DetectResponse> result(@PathVariable String imageId) {
        return ResponseEntity.ok(detectionService.getLatestByImage(imageId));
    }
}
