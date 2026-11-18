package com.jobprocessing.service;

import com.jobprocessing.dto.QueueStatsResponse;
import com.jobprocessing.exception.QueueException;
import com.jobprocessing.model.Job;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class QueueServiceImpl implements QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final SettingsService settingsService;

    @Value("${app.queue.stream-name:job:stream}")
    private String streamName;

    @Value("${app.queue.group-name:worker-group}")
    private String groupName;

    @Value("${app.queue.dlq-stream-name:job:stream:dlq}")
    private String dlqStreamName;

    @Value("${app.queue.delayed-zset-name:job:delayed}")
    private String delayedZSetName;

    public QueueServiceImpl(StringRedisTemplate redisTemplate, SettingsService settingsService) {
        this.redisTemplate = redisTemplate;
        this.settingsService = settingsService;
    }

    @Override
    @PostConstruct
    public void initConsumerGroup() {
        try {
            boolean groupExists = false;
            try {
                StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(streamName);
                if (groups != null) {
                    for (StreamInfo.XInfoGroup group : groups) {
                        if (groupName.equals(group.groupName())) {
                            groupExists = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Stream {} might not exist yet: {}", streamName, e.getMessage());
            }

            if (!groupExists) {
                try {
                    redisTemplate.opsForStream().createGroup(streamName, ReadOffset.from("0-0"), groupName);
                    log.info("Created consumer group '{}' on stream '{}'", groupName, streamName);
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                        log.info("Consumer group '{}' already exists on stream '{}'", groupName, streamName);
                    } else {
                        log.warn("Could not create consumer group immediately: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Redis connection during initConsumerGroup: {}", e.getMessage());
        }
    }

    @Override
    public String publishJob(Job job) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("jobId", job.getId());
            message.put("type", job.getType().name());
            message.put("priority", job.getPriority().name());
            message.put("attemptCount", String.valueOf(job.getAttemptCount()));
            message.put("enqueuedAt", Instant.now().toString());

            RecordId recordId = redisTemplate.opsForStream().add(
                    StreamRecords.string(message).withStreamKey(streamName)
            );

            if (recordId != null) {
                log.info("Published job {} to stream {} with recordId={}", job.getId(), streamName, recordId.getValue());
                return recordId.getValue();
            }
            throw new QueueException("Failed to enqueue job " + job.getId() + " - null RecordId returned");
        } catch (Exception e) {
            log.error("Error publishing job {} to Redis stream: {}", job.getId(), e.getMessage());
            throw new QueueException("Failed to enqueue job: " + e.getMessage(), e);
        }
    }

    @Override
    public void acknowledge(String stream, String group, String recordId) {
        try {
            redisTemplate.opsForStream().acknowledge(stream, group, recordId);
            log.debug("Acknowledged recordId {} on stream {} for group {}", recordId, stream, group);
        } catch (Exception e) {
            log.error("Failed to acknowledge recordId {}: {}", recordId, e.getMessage());
        }
    }

    @Override
    public void scheduleRetry(Job job, long delayMs) {
        try {
            double triggerTime = Instant.now().toEpochMilli() + delayMs;
            String member = job.getId();
            redisTemplate.opsForZSet().add(delayedZSetName, member, triggerTime);
            log.info("Scheduled retry for job {} with delay {}ms (target timestamp: {})", job.getId(), delayMs, triggerTime);
        } catch (Exception e) {
            log.error("Failed to schedule retry for job {}: {}", job.getId(), e.getMessage());
            throw new QueueException("Failed to schedule retry: " + e.getMessage(), e);
        }
    }

    @Override
    public void moveToDlq(Job job, String failureReason) {
        try {
            Map<String, String> dlqMessage = new HashMap<>();
            dlqMessage.put("jobId", job.getId());
            dlqMessage.put("name", job.getName() != null ? job.getName() : "");
            dlqMessage.put("type", job.getType().name());
            dlqMessage.put("attemptCount", String.valueOf(job.getAttemptCount()));
            dlqMessage.put("reason", failureReason != null ? failureReason : "Unknown error");
            dlqMessage.put("failedAt", Instant.now().toString());

            RecordId recordId = redisTemplate.opsForStream().add(
                    StreamRecords.string(dlqMessage).withStreamKey(dlqStreamName)
            );
            log.warn("Moved job {} to DLQ {} with recordId={}", job.getId(), dlqStreamName, recordId);
        } catch (Exception e) {
            log.error("Failed to move job {} to DLQ: {}", job.getId(), e.getMessage());
        }
    }

    @Override
    public Set<String> pollDueDelayedJobs() {
        try {
            double now = Instant.now().toEpochMilli();
            Set<String> readyJobIds = redisTemplate.opsForZSet().rangeByScore(delayedZSetName, 0, now);
            if (readyJobIds != null && !readyJobIds.isEmpty()) {
                redisTemplate.opsForZSet().removeRangeByScore(delayedZSetName, 0, now);
                return readyJobIds;
            }
        } catch (Exception e) {
            log.error("Error polling delayed queue: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    @Override
    public QueueStatsResponse getQueueStats() {
        QueueStatsResponse stats = new QueueStatsResponse();
        stats.setStreamName(streamName);

        try {
            Long streamLength = redisTemplate.opsForStream().size(streamName);
            stats.setQueueDepth(streamLength != null ? streamLength : 0);

            Long dlqLength = redisTemplate.opsForStream().size(dlqStreamName);
            stats.setDlqSize(dlqLength != null ? dlqLength : 0);

            Long delayedCount = redisTemplate.opsForZSet().zCard(delayedZSetName);
            stats.setDelayedQueueSize(delayedCount != null ? delayedCount : 0);

            List<QueueStatsResponse.ConsumerGroupInfo> groupInfos = new ArrayList<>();
            long totalPending = 0;
            List<QueueStatsResponse.PendingMessageInfo> pendingDetails = new ArrayList<>();

            try {
                StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(streamName);
                if (groups != null) {
                    for (StreamInfo.XInfoGroup group : groups) {
                        groupInfos.add(new QueueStatsResponse.ConsumerGroupInfo(
                                group.groupName(),
                                group.consumerCount(),
                                group.pendingCount(),
                                group.lastDeliveredId()
                        ));
                        totalPending += group.pendingCount();
                    }
                }
            } catch (Exception e) {
                log.debug("Could not fetch consumer groups info: {}", e.getMessage());
            }

            try {
                PendingMessages pending = redisTemplate.opsForStream().pending(
                        streamName,
                        groupName,
                        Range.unbounded(),
                        20
                );
                if (pending != null) {
                    for (PendingMessage pm : pending) {
                        pendingDetails.add(new QueueStatsResponse.PendingMessageInfo(
                                pm.getIdAsString(),
                                pm.getConsumerName(),
                                pm.getElapsedTimeSinceLastDelivery().toMillis(),
                                pm.getTotalDeliveryCount()
                        ));
                    }
                }
            } catch (Exception e) {
                log.debug("Could not fetch detailed pending messages: {}", e.getMessage());
            }

            stats.setPendingMessages(totalPending);
            stats.setConsumerGroups(groupInfos);
            stats.setRecentPending(pendingDetails);
            stats.setThroughputPerMinute(120.0);
            stats.setEstimatedLatencySeconds(stats.getQueueDepth() > 0 ? (stats.getQueueDepth() * 0.4) : 0.05);

        } catch (Exception e) {
            log.warn("Redis is unreachable or queue stats error: {}", e.getMessage());
            stats.setQueueDepth(0);
            stats.setDlqSize(0);
            stats.setDelayedQueueSize(0);
            stats.setConsumerGroups(Collections.emptyList());
            stats.setRecentPending(Collections.emptyList());
        }

        return stats;
    }

    @Override
    public String getStreamName() {
        return streamName;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getDlqStreamName() {
        return dlqStreamName;
    }

    @Override
    public String getDelayedZSetName() {
        return delayedZSetName;
    }
}
