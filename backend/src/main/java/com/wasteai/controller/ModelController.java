package com.wasteai.controller;

import com.wasteai.dto.ModelUploadResponse;
import com.wasteai.service.ModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ModelUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(modelService.uploadAndUse(file));
    }

    @GetMapping("/current")
    public ResponseEntity<ModelUploadResponse> current() {
        return ResponseEntity.ok(modelService.getCurrentModel());
    }
}
