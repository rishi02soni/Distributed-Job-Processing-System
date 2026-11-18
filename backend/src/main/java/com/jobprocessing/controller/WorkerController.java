package com.jobprocessing.controller;

import com.jobprocessing.dto.ApiResponse;
import com.jobprocessing.dto.WorkerResponse;
import com.jobprocessing.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workers")
@Tag(name = "Worker Management", description = "Monitor worker nodes, concurrency, heartbeat, and utilization")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    @Operation(summary = "List all workers", description = "Retrieve status, capacity, and resource metrics for all registered worker nodes")
    public ResponseEntity<ApiResponse<List<WorkerResponse>>> getWorkers() {
        List<WorkerResponse> workers = workerService.getAllWorkers();
        return ResponseEntity.ok(ApiResponse.ok(workers));
    }
}
