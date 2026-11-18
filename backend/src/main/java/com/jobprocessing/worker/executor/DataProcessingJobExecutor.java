package com.jobprocessing.worker.executor;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobType;
import com.jobprocessing.service.JobLogService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataProcessingJobExecutor implements JobExecutor {

    @Override
    public JobType getSupportedType() {
        return JobType.DATA_PROCESSING;
    }

    @Override
    public Map<String, Object> execute(Job job, JobLogService logService) throws Exception {
        Map<String, Object> payload = job.getPayload();
        int batchSize = payload.get("batchSize") instanceof Number ? ((Number) payload.get("batchSize")).intValue() : 1000;
        String transformation = (String) payload.getOrDefault("transformation", "NORMALIZE_ETL");
        Boolean failSimulated = Boolean.TRUE.equals(payload.get("simulateFailure"));

        logService.logInfo(job.getId(), "Initializing ETL pipeline for transformation: " + transformation);
        Thread.sleep(300);

        if (failSimulated) {
            logService.logError(job.getId(), "Schema validation error on batch record 42", null);
            throw new IllegalArgumentException("Invalid record format at partition 2, offset 42: null pointer in foreign key");
        }

        logService.logInfo(job.getId(), "Processing chunk 1/3: 334 records transformed");
        Thread.sleep(200);
        logService.logInfo(job.getId(), "Processing chunk 2/3: 333 records transformed");
        Thread.sleep(200);
        logService.logInfo(job.getId(), "Processing chunk 3/3: 333 records transformed");
        Thread.sleep(200);

        Map<String, Object> result = new HashMap<>();
        result.put("transformation", transformation);
        result.put("processedRecords", batchSize);
        result.put("validRecords", batchSize);
        result.put("corruptedRecords", 0);
        result.put("status", "SUCCESS");
        return result;
    }
}
