package com.jobprocessing.worker;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobStatus;
import com.jobprocessing.repository.JobRepository;
import com.jobprocessing.service.JobLogService;
import com.jobprocessing.service.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Component
public class DelayedQueueScheduler {

    private static final Logger log = LoggerFactory.getLogger(DelayedQueueScheduler.class);

    private final QueueService queueService;
    private final JobRepository jobRepository;
    private final JobLogService logService;

    public DelayedQueueScheduler(QueueService queueService, JobRepository jobRepository, JobLogService logService) {
        this.queueService = queueService;
        this.jobRepository = jobRepository;
        this.logService = logService;
    }

    @Scheduled(fixedDelay = 1000)
    public void processDelayedJobs() {
        Set<String> readyJobIds = queueService.pollDueDelayedJobs();
        if (readyJobIds == null || readyJobIds.isEmpty()) {
            return;
        }

        for (String jobId : readyJobIds) {
            try {
                Optional<Job> jobOpt = jobRepository.findById(jobId);
                if (jobOpt.isPresent()) {
                    Job job = jobOpt.get();
                    if (job.getStatus() == JobStatus.RETRYING || job.getStatus() == JobStatus.QUEUED) {
                        job.setStatus(JobStatus.QUEUED);
                        job.setUpdatedAt(Instant.now());
                        jobRepository.save(job);

                        queueService.publishJob(job);
                        logService.logInfo(jobId, "Retry backoff delay elapsed. Re-enqueued to active stream.");
                    }
                }
            } catch (Exception e) {
                log.error("Failed to re-enqueue delayed job {}: {}", jobId, e.getMessage());
            }
        }
    }
}
