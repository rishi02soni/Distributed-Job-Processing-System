package com.jobprocessing.controller;

import com.jobprocessing.dto.AnalyticsResponse;
import com.jobprocessing.dto.ApiResponse;
import com.jobprocessing.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Performance analytics, throughput trends, latency percentiles, and failure classification")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    @Operation(summary = "Get system analytics", description = "Retrieve throughput over time, p50/p90/p99 latency metrics, type distribution, and worker performance")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics() {
        AnalyticsResponse response = analyticsService.getAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
