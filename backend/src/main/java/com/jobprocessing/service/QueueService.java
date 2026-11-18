package com.jobprocessing.service;

import com.jobprocessing.dto.QueueStatsResponse;
import com.jobprocessing.model.Job;

import java.util.Set;

public interface QueueService {

    void initConsumerGroup();

    String publishJob(Job job);

    void acknowledge(String stream, String group, String recordId);

    void scheduleRetry(Job job, long delayMs);

    void moveToDlq(Job job, String failureReason);

    Set<String> pollDueDelayedJobs();

    QueueStatsResponse getQueueStats();

    String getStreamName();

    String getGroupName();

    String getDlqStreamName();

    String getDelayedZSetName();
}
