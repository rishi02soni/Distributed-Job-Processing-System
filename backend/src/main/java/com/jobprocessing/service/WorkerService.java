package com.jobprocessing.service;

import com.jobprocessing.dto.WorkerResponse;
import com.jobprocessing.model.WorkerNode;

import java.util.List;

public interface WorkerService {

    WorkerNode registerWorker(String workerId, String hostname, int concurrency);

    void updateHeartbeat(String workerId, int activeJobs, long completed, long failed, double cpu, double mem);

    List<WorkerResponse> getAllWorkers();

    void markWorkerDead(String workerId);

    void evictDeadWorkers();
}
