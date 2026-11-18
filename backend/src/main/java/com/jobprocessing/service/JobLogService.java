package com.jobprocessing.service;

import com.jobprocessing.model.JobLog;

import java.util.List;
import java.util.Map;

public interface JobLogService {

    void log(String jobId, String level, String message, Map<String, Object> details);

    void logInfo(String jobId, String message);

    void logWarn(String jobId, String message);

    void logError(String jobId, String message, Map<String, Object> details);

    List<JobLog> getLogsForJob(String jobId);
}
