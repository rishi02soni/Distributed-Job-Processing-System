package com.jobprocessing.worker.executor;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobType;
import com.jobprocessing.service.JobLogService;

import java.util.Map;

public interface JobExecutor {

    JobType getSupportedType();

    Map<String, Object> execute(Job job, JobLogService logService) throws Exception;
}
