package com.wasteai.controller;

import com.wasteai.dto.OverviewStatsResponse;
import com.wasteai.service.OverviewStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final OverviewStatsService overviewStatsService;

    public StatsController(OverviewStatsService overviewStatsService) {
        this.overviewStatsService = overviewStatsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<OverviewStatsResponse> overview() {
        return ResponseEntity.ok(overviewStatsService.getOverview());
    }
}
