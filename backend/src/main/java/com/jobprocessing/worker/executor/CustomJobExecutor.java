package com.jobprocessing.worker.executor;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobType;
import com.jobprocessing.service.JobLogService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomJobExecutor implements JobExecutor {

    @Override
    public JobType getSupportedType() {
        return JobType.CUSTOM;
    }

    @Override
    public Map<String, Object> execute(Job job, JobLogService logService) throws Exception {
        Map<String, Object> payload = job.getPayload();
        String action = (String) payload.getOrDefault("action", "EXECUTE_CUSTOM_TASK");
        Boolean failSimulated = Boolean.TRUE.equals(payload.get("simulateFailure"));

        logService.logInfo(job.getId(), "Starting custom action: " + action);
        Thread.sleep(300);

        if (failSimulated) {
            String errorMsg = (String) payload.getOrDefault("errorMessage", "Simulated fatal failure in custom task executor");
            logService.logError(job.getId(), errorMsg, null);
            throw new RuntimeException(errorMsg);
        }

        logService.logInfo(job.getId(), "Custom action executed with parameters: " + payload.keySet());
        Thread.sleep(300);

        Map<String, Object> result = new HashMap<>();
        result.put("action", action);
        result.put("status", "SUCCESS");
        result.put("processedPayloadKeys", payload.keySet().size());
        return result;
    }
}
