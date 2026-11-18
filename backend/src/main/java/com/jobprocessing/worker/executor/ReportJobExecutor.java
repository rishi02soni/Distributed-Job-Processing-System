package com.jobprocessing.worker.executor;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobType;
import com.jobprocessing.service.JobLogService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ReportJobExecutor implements JobExecutor {

    @Override
    public JobType getSupportedType() {
        return JobType.REPORT;
    }

    @Override
    public Map<String, Object> execute(Job job, JobLogService logService) throws Exception {
        Map<String, Object> payload = job.getPayload();
        String reportName = (String) payload.getOrDefault("reportName", "Monthly Financial Summary");
        String format = (String) payload.getOrDefault("format", "PDF");
        Boolean failSimulated = Boolean.TRUE.equals(payload.get("simulateFailure"));

        logService.logInfo(job.getId(), "Compiling data query for report: '" + reportName + "' (" + format + ")");
        Thread.sleep(400);

        if (failSimulated) {
            logService.logError(job.getId(), "Data warehouse query timeout during aggregation", null);
            throw new RuntimeException("Data Warehouse Query Timeout: Lock acquisition failed");
        }

        logService.logInfo(job.getId(), "Fetched 14,250 records. Generating charts and tables.");
        Thread.sleep(500);

        String fileId = "rpt_" + UUID.randomUUID().toString().substring(0, 10);
        String downloadUrl = "https://storage.internal/reports/" + fileId + "." + format.toLowerCase();
        logService.logInfo(job.getId(), "Report generated and uploaded to " + downloadUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("reportName", reportName);
        result.put("format", format);
        result.put("rowCount", 14250);
        result.put("fileSizeKb", 845);
        result.put("downloadUrl", downloadUrl);
        return result;
    }
}
