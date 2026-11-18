package com.jobprocessing.repository;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobStatus;
import com.jobprocessing.model.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {

    Optional<Job> findByIdempotencyKey(String idempotencyKey);

    long countByStatus(JobStatus status);

    long countByType(JobType type);

    List<Job> findByStatus(JobStatus status);

    List<Job> findByStatusAndStartedAtBefore(JobStatus status, Instant cutoffTime);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    Page<Job> findByType(JobType type, Pageable pageable);
}
