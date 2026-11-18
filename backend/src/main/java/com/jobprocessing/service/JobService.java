package com.jobprocessing.service;

import com.jobprocessing.dto.CreateJobRequest;
import com.jobprocessing.dto.JobResponse;
import com.jobprocessing.dto.JobStatsResponse;
import com.jobprocessing.dto.PagedResponse;
import com.jobprocessing.model.JobPriority;
import com.jobprocessing.model.JobStatus;
import com.jobprocessing.model.JobType;

import java.time.Instant;

public interface JobService {

    JobResponse submitJob(CreateJobRequest request, String idempotencyKeyHeader);

    PagedResponse<JobResponse> listJobs(
            JobStatus status,
            JobType type,
            JobPriority priority,
            String search,
            Instant startDate,
            Instant endDate,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    JobResponse getJob(String id);

    JobResponse retryJob(String id);

    JobResponse cancelJob(String id);

    JobStatsResponse getJobStats();
}
