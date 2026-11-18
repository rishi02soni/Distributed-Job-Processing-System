package com.jobprocessing.worker;

import com.jobprocessing.service.WorkerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

@Component
public class WorkerHeartbeatScheduler {

    private final WorkerService workerService;
    private final WorkerManager workerManager;

    public WorkerHeartbeatScheduler(WorkerService workerService, WorkerManager workerManager) {
        this.workerService = workerService;
        this.workerManager = workerManager;
    }

    @Scheduled(fixedRateString = "${app.worker.heartbeat-interval-ms:5000}")
    public void reportHeartbeat() {
        String workerId = workerManager.getWorkerId();
        if (workerId == null) {
            return;
        }

        double cpu = 0.0;
        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            cpu = Math.max(0.0, osBean.getCpuLoad() * 100.0);
        } catch (Exception ignored) {
            cpu = 12.5; // fallback simulated metric
        }

        Runtime runtime = Runtime.getRuntime();
        double memory = ((double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory()) * 100.0;

        workerService.updateHeartbeat(
                workerId,
                workerManager.getActiveJobs(),
                workerManager.getCompletedCount(),
                workerManager.getFailedCount(),
                Math.round(cpu * 10.0) / 10.0,
                Math.round(memory * 10.0) / 10.0
        );
    }
}
