package com.jobprocessing.service;

import com.jobprocessing.dto.CreateJobRequest;
import com.jobprocessing.dto.JobResponse;
import com.jobprocessing.dto.JobStatsResponse;
import com.jobprocessing.dto.PagedResponse;
import com.jobprocessing.exception.InvalidJobStateException;
import com.jobprocessing.exception.ResourceNotFoundException;
import com.jobprocessing.model.*;
import com.jobprocessing.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    private final JobRepository jobRepository;
    private final MongoOperations mongoTemplate;
    private final QueueService queueService;
    private final JobLogService logService;
    private final SettingsService settingsService;
    private final WorkerService workerService;

    public JobServiceImpl(
            JobRepository jobRepository,
            MongoOperations mongoTemplate,
            QueueService queueService,
            JobLogService logService,
            SettingsService settingsService,
            WorkerService workerService
    ) {
        this.jobRepository = jobRepository;
        this.mongoTemplate = mongoTemplate;
        this.queueService = queueService;
        this.logService = logService;
        this.settingsService = settingsService;
        this.workerService = workerService;
    }

    @Override
    public JobResponse submitJob(CreateJobRequest request, String idempotencyKeyHeader) {
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = idempotencyKeyHeader;
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Job> existing = jobRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Returning existing job with idempotency key {}", idempotencyKey);
                return JobResponse.fromEntity(existing.get());
            }
        }

        Job job = new Job();
        job.setId("job_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        job.setName(request.getName());
        job.setType(request.getType());
        job.setPriority(request.getPriority() != null ? request.getPriority() : JobPriority.NORMAL);
        job.setPayload(request.getPayload() != null ? request.getPayload() : new HashMap<>());
        job.setIdempotencyKey(idempotencyKey != null && !idempotencyKey.isBlank() ? idempotencyKey : null);
        job.setMaxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : settingsService.getIntSetting("defaultMaxRetries", 3));
        job.setTimeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : settingsService.getIntSetting("jobTimeoutSeconds", 60));
        job.setScheduledAt(request.getScheduledAt());
        job.setStatus(JobStatus.QUEUED);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());

        Job savedJob = jobRepository.save(job);
        logService.logInfo(savedJob.getId(), "Job submitted with status QUEUED");

        // Delayed or immediate execution
        if (job.getScheduledAt() != null && job.getScheduledAt().isAfter(Instant.now())) {
            long delayMs = Duration.between(Instant.now(), job.getScheduledAt()).toMillis();
            queueService.scheduleRetry(savedJob, delayMs);
            logService.logInfo(savedJob.getId(), "Job execution scheduled for " + job.getScheduledAt());
        } else {
            try {
                queueService.publishJob(savedJob);
                logService.logInfo(savedJob.getId(), "Job successfully pushed to Redis Stream");
            } catch (Exception e) {
                log.warn("Job saved in MongoDB, but queue push failed: {}", e.getMessage());
                logService.logWarn(savedJob.getId(), "Enqueued in MongoDB; Redis Stream delayed: " + e.getMessage());
            }
        }

        return JobResponse.fromEntity(savedJob);
    }

    @Override
    public PagedResponse<JobResponse> listJobs(
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
    ) {
        Query query = new Query();

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (priority != null) {
            query.addCriteria(Criteria.where("priority").is(priority));
        }
        if (search != null && !search.isBlank()) {
            Pattern pattern = Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE);
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("id").regex(pattern),
                    Criteria.where("name").regex(pattern)
            ));
        }
        if (startDate != null || endDate != null) {
            Criteria dateCriteria = Criteria.where("createdAt");
            if (startDate != null) {
                dateCriteria = dateCriteria.gte(startDate);
            }
            if (endDate != null) {
                dateCriteria = dateCriteria.lte(endDate);
            }
            query.addCriteria(dateCriteria);
        }

        long total = mongoTemplate.count(query, Job.class);

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        query.with(pageable);
        List<Job> jobs = mongoTemplate.find(query, Job.class);

        List<JobResponse> responses = jobs.stream()
                .map(JobResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.of(new PageImpl<>(responses, pageable, total));
    }

    @Override
    public JobResponse getJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return JobResponse.fromEntity(job);
    }

    @Override
    public JobResponse retryJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        if (job.getStatus() != JobStatus.FAILED && job.getStatus() != JobStatus.CANCELLED) {
            throw new InvalidJobStateException("Cannot retry job in status: " + job.getStatus() + ". Only FAILED or CANCELLED jobs can be retried.");
        }

        job.setStatus(JobStatus.QUEUED);
        job.setErrorDetails(null);
        job.setAttemptCount(0);
        job.setStartedAt(null);
        job.setCompletedAt(null);
        job.setDurationMs(null);
        job.setWorkerId(null);
        job.setUpdatedAt(Instant.now());

        Job updated = jobRepository.save(job);
        logService.logInfo(id, "Job manually retried and re-enqueued");

        try {
            queueService.publishJob(updated);
        } catch (Exception e) {
            log.error("Failed to republish retried job: {}", e.getMessage());
        }

        return JobResponse.fromEntity(updated);
    }

    @Override
    public JobResponse cancelJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        if (job.getStatus() == JobStatus.COMPLETED) {
            throw new InvalidJobStateException("Cannot cancel an already COMPLETED job.");
        }
        if (job.getStatus() == JobStatus.CANCELLED) {
            return JobResponse.fromEntity(job);
        }

        job.setStatus(JobStatus.CANCELLED);
        job.setCompletedAt(Instant.now());
        if (job.getStartedAt() != null) {
            job.setDurationMs(Duration.between(job.getStartedAt(), job.getCompletedAt()).toMillis());
        }
        job.setUpdatedAt(Instant.now());

        Job saved = jobRepository.save(job);
        logService.logWarn(id, "Job cancelled by user request");
        return JobResponse.fromEntity(saved);
    }

    @Override
    public JobStatsResponse getJobStats() {
        JobStatsResponse stats = new JobStatsResponse();

        long total = jobRepository.count();
        long queued = jobRepository.countByStatus(JobStatus.QUEUED);
        long running = jobRepository.countByStatus(JobStatus.RUNNING);
        long completed = jobRepository.countByStatus(JobStatus.COMPLETED);
        long failed = jobRepository.countByStatus(JobStatus.FAILED);
        long retrying = jobRepository.countByStatus(JobStatus.RETRYING);
        long cancelled = jobRepository.countByStatus(JobStatus.CANCELLED);

        stats.setTotalJobs(total);
        stats.setQueuedJobs(queued);
        stats.setRunningJobs(running);
        stats.setCompletedJobs(completed);
        stats.setFailedJobs(failed);
        stats.setRetryingJobs(retrying);
        stats.setCancelledJobs(cancelled);

        long finished = completed + failed;
        stats.setSuccessRate(finished > 0 ? ((double) completed / finished) * 100.0 : 100.0);

        // Average duration calculation from recent completed jobs
        Query completedQuery = new Query(Criteria.where("status").is(JobStatus.COMPLETED))
                .limit(100);
        List<Job> completedJobs = mongoTemplate.find(completedQuery, Job.class);
        double avgDuration = completedJobs.stream()
                .filter(j -> j.getDurationMs() != null)
                .mapToLong(Job::getDurationMs)
                .average()
                .orElse(0.0);
        stats.setAvgProcessingTimeMs(avgDuration);

        // Active workers count
        stats.setActiveWorkers(workerService.getAllWorkers().stream().filter(w -> w.isHealthy()).count());

        // Queue depth
        stats.setQueueDepth(queueService.getQueueStats().getQueueDepth());

        return stats;
    }
}
