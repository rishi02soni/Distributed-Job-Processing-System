package com.jobprocessing.controller;

import com.jobprocessing.dto.*;
import com.jobprocessing.model.JobLog;
import com.jobprocessing.model.JobPriority;
import com.jobprocessing.model.JobStatus;
import com.jobprocessing.model.JobType;
import com.jobprocessing.service.JobLogService;
import com.jobprocessing.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Job Management", description = "Endpoints for submitting, querying, retrying, and cancelling distributed jobs")
public class JobController {

    private final JobService jobService;
    private final JobLogService logService;

    public JobController(JobService jobService, JobLogService logService) {
        this.jobService = jobService;
        this.logService = logService;
    }

    @PostMapping
    @Operation(summary = "Submit a new job", description = "Submits an asynchronous job with payload, priority, and optional idempotency key")
    public ResponseEntity<ApiResponse<JobResponse>> submitJob(
            @Valid @RequestBody CreateJobRequest request,
            @Parameter(description = "Optional idempotency key to prevent duplicate processing")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader
    ) {
        JobResponse response = jobService.submitJob(request, idempotencyKeyHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Job submitted successfully", response));
    }

    @GetMapping
    @Operation(summary = "List jobs", description = "Query jobs with pagination, multi-criteria filtering, and sorting")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> listJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) JobType type,
            @RequestParam(required = false) JobPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        PagedResponse<JobResponse> result = jobService.listJobs(
                status, type, priority, search, startDate, endDate, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job details", description = "Retrieve full metadata, payload, result, and error details of a specific job")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(@PathVariable String id) {
        JobResponse response = jobService.getJob(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "Get job execution logs", description = "Retrieve chronological structured log messages recorded during job execution")
    public ResponseEntity<ApiResponse<List<JobLog>>> getJobLogs(@PathVariable String id) {
        List<JobLog> logs = logService.getLogsForJob(id);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry a failed or cancelled job", description = "Re-queues a failed or cancelled job for execution")
    public ResponseEntity<ApiResponse<JobResponse>> retryJob(@PathVariable String id) {
        JobResponse response = jobService.retryJob(id);
        return ResponseEntity.ok(ApiResponse.ok("Job queued for retry", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a job", description = "Aborts an active or queued job")
    public ResponseEntity<ApiResponse<JobResponse>> cancelJob(@PathVariable String id) {
        JobResponse response = jobService.cancelJob(id);
        return ResponseEntity.ok(ApiResponse.ok("Job cancelled", response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get overall job statistics", description = "Aggregated counts of total, queued, running, completed, failed jobs, and success rate")
    public ResponseEntity<ApiResponse<JobStatsResponse>> getJobStats() {
        JobStatsResponse stats = jobService.getJobStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
