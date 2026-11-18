package com.jobprocessing.controller;

import com.jobprocessing.dto.ApiResponse;
import com.jobprocessing.dto.QueueStatsResponse;
import com.jobprocessing.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queue")
@Tag(name = "Queue Telemetry", description = "Redis Streams metrics, consumer group lag, and DLQ status")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get queue telemetry", description = "Returns Redis stream queue depth, pending entries list, DLQ size, and delayed queue depth")
    public ResponseEntity<ApiResponse<QueueStatsResponse>> getQueueStats() {
        QueueStatsResponse stats = queueService.getQueueStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
