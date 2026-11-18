package com.jobprocessing.worker;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobStatus;
import com.jobprocessing.dto.WorkerResponse;
import com.jobprocessing.repository.JobRepository;
import com.jobprocessing.service.JobLogService;
import com.jobprocessing.service.QueueService;
import com.jobprocessing.service.WorkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WorkerRecoveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(WorkerRecoveryScheduler.class);

    private final WorkerService workerService;
    private final JobRepository jobRepository;
    private final QueueService queueService;
    private final JobLogService logService;

    @Value("${app.worker.job-timeout-seconds:60}")
    private int defaultJobTimeoutSeconds;

    public WorkerRecoveryScheduler(
            WorkerService workerService,
            JobRepository jobRepository,
            QueueService queueService,
            JobLogService logService
    ) {
        this.workerService = workerService;
        this.jobRepository = jobRepository;
        this.queueService = queueService;
        this.logService = logService;
    }

    @Scheduled(fixedDelayString = "${app.worker.recovery-interval-ms:10000}")
    public void recoverOrphanedJobsAndEvictDeadWorkers() {
        // 1. Evict stale workers
        workerService.evictDeadWorkers();

        // 2. Identify dead workers IDs
        List<WorkerResponse> workers = workerService.getAllWorkers();
        Set<String> deadWorkerIds = workers.stream()
                .filter(w -> !w.isHealthy())
                .map(WorkerResponse::getWorkerId)
                .collect(Collectors.toSet());

        // 3. Scan for jobs currently marked as RUNNING
        List<Job> runningJobs = jobRepository.findByStatus(JobStatus.RUNNING);
        Instant now = Instant.now();

        for (Job job : runningJobs) {
            boolean isAssignedToDeadWorker = job.getWorkerId() != null && deadWorkerIds.contains(job.getWorkerId());
            int timeout = job.getTimeoutSeconds() > 0 ? job.getTimeoutSeconds() : defaultJobTimeoutSeconds;
            boolean isTimedOut = job.getStartedAt() != null && job.getStartedAt().plusSeconds(timeout).isBefore(now);

            if (isAssignedToDeadWorker || isTimedOut) {
                String reason = isAssignedToDeadWorker
                        ? "Worker [" + job.getWorkerId() + "] became unresponsive/dead"
                        : "Execution timed out after " + timeout + " seconds";

                log.warn("Recovering orphaned job {}: {}", job.getId(), reason);

                if (job.getAttemptCount() < job.getMaxRetries()) {
                    job.setStatus(JobStatus.QUEUED);
                    job.setWorkerId(null);
                    job.setStartedAt(null);
                    job.setUpdatedAt(now);
                    jobRepository.save(job);

                    try {
                        queueService.publishJob(job);
                        logService.logWarn(job.getId(), "Orphan recovery: " + reason + ". Job re-queued for attempt " + (job.getAttemptCount() + 1));
                    } catch (Exception e) {
                        log.error("Failed to re-enqueue recovered job {}: {}", job.getId(), e.getMessage());
                    }
                } else {
                    job.setStatus(JobStatus.FAILED);
                    job.setCompletedAt(now);
                    job.setUpdatedAt(now);
                    jobRepository.save(job);

                    queueService.moveToDlq(job, reason);
                    logService.logError(job.getId(), "Orphan recovery: " + reason + ". Retries exhausted, moved to DLQ.", Map.of("reason", reason));
                }
            }
        }
    }
}
