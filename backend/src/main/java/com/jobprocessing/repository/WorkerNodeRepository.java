package com.jobprocessing.repository;

import com.jobprocessing.model.WorkerNode;
import com.jobprocessing.model.WorkerStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface WorkerNodeRepository extends MongoRepository<WorkerNode, String> {

    List<WorkerNode> findByStatus(WorkerStatus status);

    List<WorkerNode> findByLastHeartbeatBefore(Instant threshold);

    List<WorkerNode> findByStatusNot(WorkerStatus status);
}
