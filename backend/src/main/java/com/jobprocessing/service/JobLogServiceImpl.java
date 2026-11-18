package com.jobprocessing.service;

import com.jobprocessing.model.JobLog;
import com.jobprocessing.repository.JobLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class JobLogServiceImpl implements JobLogService {

    private static final Logger log = LoggerFactory.getLogger(JobLogServiceImpl.class);
    private final JobLogRepository logRepository;

    public JobLogServiceImpl(JobLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public void log(String jobId, String level, String message, Map<String, Object> details) {
        try {
            JobLog jobLog = new JobLog(jobId, level, message, details);
            jobLog.setTimestamp(Instant.now());
            logRepository.save(jobLog);
            log.debug("[Job {}] [{}]: {}", jobId, level, message);
        } catch (Exception e) {
            log.error("Failed to persist job log for jobId={}: {}", jobId, e.getMessage());
        }
    }

    @Override
    public void logInfo(String jobId, String message) {
        log(jobId, "INFO", message, null);
    }

    @Override
    public void logWarn(String jobId, String message) {
        log(jobId, "WARN", message, null);
    }

    @Override
    public void logError(String jobId, String message, Map<String, Object> details) {
        log(jobId, "ERROR", message, details);
    }

    @Override
    public List<JobLog> getLogsForJob(String jobId) {
        return logRepository.findByJobIdOrderByTimestampAsc(jobId);
    }
}
