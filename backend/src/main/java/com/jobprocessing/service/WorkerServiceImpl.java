package com.jobprocessing.service;

import com.jobprocessing.dto.WorkerResponse;
import com.jobprocessing.model.WorkerNode;
import com.jobprocessing.model.WorkerStatus;
import com.jobprocessing.repository.WorkerNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkerServiceImpl implements WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);
    private final WorkerNodeRepository workerRepository;

    @Value("${app.worker.dead-worker-threshold-seconds:30}")
    private int deadWorkerThresholdSeconds;

    public WorkerServiceImpl(WorkerNodeRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @Override
    public WorkerNode registerWorker(String workerId, String hostname, int concurrency) {
        Optional<WorkerNode> existing = workerRepository.findById(workerId);
        WorkerNode node = existing.orElseGet(() -> new WorkerNode(workerId, hostname, concurrency));
        node.setHostname(hostname);
        node.setConcurrency(concurrency);
        node.setStatus(WorkerStatus.ACTIVE);
        node.setLastHeartbeat(Instant.now());
        WorkerNode saved = workerRepository.save(node);
        log.info("Worker node registered: workerId={}, hostname={}, concurrency={}", workerId, hostname, concurrency);
        return saved;
    }

    @Override
    public void updateHeartbeat(String workerId, int activeJobs, long completed, long failed, double cpu, double mem) {
        workerRepository.findById(workerId).ifPresent(node -> {
            node.setLastHeartbeat(Instant.now());
            node.setActiveJobs(activeJobs);
            node.setCompletedJobs(completed);
            node.setFailedJobs(failed);
            node.setCpuUsage(cpu);
            node.setMemoryUsage(mem);
            node.setStatus(activeJobs > 0 ? WorkerStatus.BUSY : WorkerStatus.ACTIVE);
            workerRepository.save(node);
        });
    }

    @Override
    public List<WorkerResponse> getAllWorkers() {
        return workerRepository.findAll().stream()
                .map(node -> WorkerResponse.fromEntity(node, deadWorkerThresholdSeconds))
                .collect(Collectors.toList());
    }

    @Override
    public void markWorkerDead(String workerId) {
        workerRepository.findById(workerId).ifPresent(node -> {
            node.setStatus(WorkerStatus.DEAD);
            node.setActiveJobs(0);
            workerRepository.save(node);
            log.warn("Worker marked DEAD: workerId={}", workerId);
        });
    }

    @Override
    public void evictDeadWorkers() {
        Instant threshold = Instant.now().minusSeconds(deadWorkerThresholdSeconds);
        List<WorkerNode> staleWorkers = workerRepository.findByLastHeartbeatBefore(threshold);
        for (WorkerNode worker : staleWorkers) {
            if (worker.getStatus() != WorkerStatus.DEAD) {
                log.warn("Heartbeat expired for worker {}. Marking as DEAD.", worker.getWorkerId());
                worker.setStatus(WorkerStatus.DEAD);
                worker.setActiveJobs(0);
                workerRepository.save(worker);
            }
        }
    }
}
