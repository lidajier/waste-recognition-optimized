package com.wasteai.controller;

import com.wasteai.dto.ExperimentRunDto;
import com.wasteai.dto.ExperimentRunRequest;
import com.wasteai.dto.ExperimentRunsResponse;
import com.wasteai.service.ExperimentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @PostMapping
    public ResponseEntity<ExperimentRunDto> create(@Valid @RequestBody ExperimentRunRequest request) {
        return ResponseEntity.ok(experimentService.create(request));
    }

    @GetMapping
    public ResponseEntity<ExperimentRunsResponse> list() {
        return ResponseEntity.ok(experimentService.list());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        experimentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
