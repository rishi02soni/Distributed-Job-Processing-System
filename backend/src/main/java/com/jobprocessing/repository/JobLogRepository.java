package com.jobprocessing.repository;

import com.jobprocessing.model.JobLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobLogRepository extends MongoRepository<JobLog, String> {

    List<JobLog> findByJobIdOrderByTimestampAsc(String jobId);

    void deleteByJobId(String jobId);
}
