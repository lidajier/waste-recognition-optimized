package com.wasteai.controller;

import com.wasteai.dto.HistoryResponse;
import com.wasteai.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public ResponseEntity<HistoryResponse> history(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) Boolean flagged,
            @RequestParam(required = false) String reviewType,
            @RequestParam(required = false) Float minConfidence
    ) {
        return ResponseEntity.ok(historyService.getRecentHistory(limit, keyword, className, favorite, flagged, reviewType, minConfidence));
    }
}
