package com.jobprocessing.worker;

import com.jobprocessing.model.*;
import com.jobprocessing.repository.JobRepository;
import com.jobprocessing.service.JobLogService;
import com.jobprocessing.service.QueueService;
import com.jobprocessing.service.SettingsService;
import com.jobprocessing.service.WorkerService;
import com.jobprocessing.worker.executor.JobExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class WorkerManager {

    private static final Logger log = LoggerFactory.getLogger(WorkerManager.class);

    private final JobRepository jobRepository;
    private final QueueService queueService;
    private final JobLogService logService;
    private final WorkerService workerService;
    private final SettingsService settingsService;
    private final StringRedisTemplate redisTemplate;
    private final Map<JobType, JobExecutor> executorMap = new EnumMap<>(JobType.class);

    @Value("${app.worker.enabled:true}")
    private boolean workerEnabled;

    @Value("${app.worker.concurrency:4}")
    private int concurrency;

    @Value("${app.queue.stream-name:job:stream}")
    private String streamName;

    @Value("${app.queue.group-name:worker-group}")
    private String groupName;

    private String workerId;
    private String hostname;
    private ExecutorService threadPool;
    private Thread consumerThread;
    private volatile boolean running = false;

    private final AtomicInteger activeJobs = new AtomicInteger(0);
    private final AtomicLong completedCount = new AtomicLong(0);
    private final AtomicLong failedCount = new AtomicLong(0);

    public WorkerManager(
            JobRepository jobRepository,
            QueueService queueService,
            JobLogService logService,
            WorkerService workerService,
            SettingsService settingsService,
            StringRedisTemplate redisTemplate,
            List<JobExecutor> executors
    ) {
        this.jobRepository = jobRepository;
        this.queueService = queueService;
        this.logService = logService;
        this.workerService = workerService;
        this.settingsService = settingsService;
        this.redisTemplate = redisTemplate;

        for (JobExecutor executor : executors) {
            this.executorMap.put(executor.getSupportedType(), executor);
        }
    }

    @PostConstruct
    public void start() {
        if (!workerEnabled) {
            log.info("Worker processing is disabled on this instance (WORKER_ENABLED=false)");
            return;
        }

        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            this.hostname = "localhost";
        }
        this.workerId = "worker-" + hostname + "-" + UUID.randomUUID().toString().substring(0, 6);
        this.threadPool = Executors.newFixedThreadPool(concurrency);
        this.running = true;

        workerService.registerWorker(workerId, hostname, concurrency);

        consumerThread = new Thread(this::consumeLoop, "redis-worker-consumer-" + workerId);
        consumerThread.setDaemon(true);
        consumerThread.start();

        log.info("WorkerManager started: workerId={}, concurrency={}, groupName={}, streamName={}",
                workerId, concurrency, groupName, streamName);
    }

    private void consumeLoop() {
        // Allow Redis and consumer group initialization
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        while (running) {
            try {
                // Read next messages from Redis Stream consumer group
                List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                        Consumer.from(groupName, workerId),
                        StreamReadOptions.empty().count(concurrency).block(Duration.ofMillis(2000)),
                        StreamOffset.create(streamName, ReadOffset.lastConsumed())
                );

                if (records != null && !records.isEmpty()) {
                    for (MapRecord<String, Object, Object> record : records) {
                        if (!running) break;
                        threadPool.submit(() -> processRecord(record));
                    }
                }
            } catch (Exception e) {
                if (running) {
                    log.debug("Redis stream read idle or disconnected: {}", e.getMessage());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    private void processRecord(MapRecord<String, Object, Object> record) {
        String recordId = record.getId().getValue();
        Map<Object, Object> valueMap = record.getValue();
        String jobId = (String) valueMap.get("jobId");

        if (jobId == null) {
            queueService.acknowledge(streamName, groupName, recordId);
            return;
        }

        activeJobs.incrementAndGet();
        try {
            Optional<Job> jobOpt = jobRepository.findById(jobId);
            if (jobOpt.isEmpty()) {
                log.warn("Job {} not found in MongoDB. Dropping message.", jobId);
                queueService.acknowledge(streamName, groupName, recordId);
                return;
            }

            Job job = jobOpt.get();

            // Guard against processing already completed or cancelled jobs
            if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) {
                log.info("Job {} is already in terminal state {}. Acknowledging.", jobId, job.getStatus());
                queueService.acknowledge(streamName, groupName, recordId);
                return;
            }

            // Update to RUNNING
            int attempt = job.getAttemptCount() + 1;
            job.setStatus(JobStatus.RUNNING);
            job.setAttemptCount(attempt);
            job.setWorkerId(workerId);
            job.setStartedAt(Instant.now());
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);

            logService.logInfo(jobId, String.format(
                    "Picked up by worker [%s] on attempt %d/%d",
                    workerId, attempt, job.getMaxRetries()
            ));

            JobExecutor executor = executorMap.get(job.getType());
            if (executor == null) {
                throw new IllegalStateException("No registered executor for job type: " + job.getType());
            }

            // Execute with watchdog timeout
            int timeoutSec = job.getTimeoutSeconds() > 0 ? job.getTimeoutSeconds() : 60;
            Future<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return executor.execute(job, logService);
                } catch (Exception ex) {
                    throw new CompletionException(ex);
                }
            });

            Map<String, Object> result = future.get(timeoutSec, TimeUnit.SECONDS);

            // SUCCESS FLOW
            Instant completedAt = Instant.now();
            long duration = Duration.between(job.getStartedAt(), completedAt).toMillis();

            job.setStatus(JobStatus.COMPLETED);
            job.setResult(result);
            job.setCompletedAt(completedAt);
            job.setDurationMs(duration);
            job.setErrorDetails(null);
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);

            queueService.acknowledge(streamName, groupName, recordId);
            completedCount.incrementAndGet();

            logService.logInfo(jobId, String.format("Job COMPLETED successfully in %d ms", duration));

        } catch (Exception e) {
            handleJobFailure(jobId, recordId, e);
        } finally {
            activeJobs.decrementAndGet();
        }
    }

    private void handleJobFailure(String jobId, String recordId, Exception exception) {
        Throwable cause = exception;
        if (exception instanceof ExecutionException && exception.getCause() != null) {
            cause = exception.getCause();
        } else if (exception instanceof TimeoutException) {
            cause = new TimeoutException("Job execution exceeded maximum timeout limit");
        }

        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw));

        ErrorDetails error = new ErrorDetails(
                cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName(),
                cause.getClass().getSimpleName(),
                sw.toString(),
                Instant.now()
        );

        failedCount.incrementAndGet();

        try {
            Optional<Job> jobOpt = jobRepository.findById(jobId);
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                job.setErrorDetails(error);
                job.setUpdatedAt(Instant.now());

                if (job.getAttemptCount() < job.getMaxRetries()) {
                    // RETRY WITH EXPONENTIAL BACKOFF
                    long baseBackoff = settingsService.getLongSetting("backoffBaseMs", 2000L);
                    double multiplier = settingsService.getDoubleSetting("backoffMultiplier", 2.0);
                    long delayMs = (long) (baseBackoff * Math.pow(multiplier, Math.max(0, job.getAttemptCount() - 1)));

                    job.setStatus(JobStatus.RETRYING);
                    jobRepository.save(job);

                    queueService.scheduleRetry(job, delayMs);
                    queueService.acknowledge(streamName, groupName, recordId);

                    logService.logWarn(jobId, String.format(
                            "Attempt %d/%d failed: %s. Re-scheduling retry in %d ms",
                            job.getAttemptCount(), job.getMaxRetries(), cause.getMessage(), delayMs
                    ));
                } else {
                    // RETRIES EXHAUSTED -> DEAD LETTER QUEUE (DLQ)
                    Instant completedAt = Instant.now();
                    job.setStatus(JobStatus.FAILED);
                    job.setCompletedAt(completedAt);
                    if (job.getStartedAt() != null) {
                        job.setDurationMs(Duration.between(job.getStartedAt(), completedAt).toMillis());
                    }
                    jobRepository.save(job);

                    queueService.moveToDlq(job, cause.getMessage());
                    queueService.acknowledge(streamName, groupName, recordId);

                    logService.logError(jobId, String.format(
                            "Job exhausted all %d retry attempts. Final status FAILED. Moved to DLQ.",
                            job.getMaxRetries()
                    ), Map.of("errorType", cause.getClass().getSimpleName(), "message", cause.getMessage() != null ? cause.getMessage() : ""));
                }
            } else {
                queueService.acknowledge(streamName, groupName, recordId);
            }
        } catch (Exception ex) {
            log.error("Error updating failure state for job {}: {}", jobId, ex.getMessage(), ex);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }
        if (workerId != null) {
            workerService.markWorkerDead(workerId);
        }
        log.info("WorkerManager stopped: {}", workerId);
    }

    public String getWorkerId() {
        return workerId;
    }

    public int getActiveJobs() {
        return activeJobs.get();
    }

    public long getCompletedCount() {
        return completedCount.get();
    }

    public long getFailedCount() {
        return failedCount.get();
    }
}
