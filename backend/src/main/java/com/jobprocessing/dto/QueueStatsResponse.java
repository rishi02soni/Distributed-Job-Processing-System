package com.jobprocessing.dto;

import java.util.List;
import java.util.Map;

public class QueueStatsResponse {

    private String streamName;
    private long queueDepth;
    private long pendingMessages;
    private long dlqSize;
    private long delayedQueueSize;
    private List<ConsumerGroupInfo> consumerGroups;
    private List<PendingMessageInfo> recentPending;
    private double throughputPerMinute;
    private double estimatedLatencySeconds;

    public QueueStatsResponse() {
    }

    public static class ConsumerGroupInfo {
        private String name;
        private long consumers;
        private long pending;
        private String lastDeliveredId;

        public ConsumerGroupInfo() {
        }

        public ConsumerGroupInfo(String name, long consumers, long pending, String lastDeliveredId) {
            this.name = name;
            this.consumers = consumers;
            this.pending = pending;
            this.lastDeliveredId = lastDeliveredId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getConsumers() {
            return consumers;
        }

        public void setConsumers(long consumers) {
            this.consumers = consumers;
        }

        public long getPending() {
            return pending;
        }

        public void setPending(long pending) {
            this.pending = pending;
        }

        public String getLastDeliveredId() {
            return lastDeliveredId;
        }

        public void setLastDeliveredId(String lastDeliveredId) {
            this.lastDeliveredId = lastDeliveredId;
        }
    }

    public static class PendingMessageInfo {
        private String id;
        private String consumerName;
        private long idleTimeMs;
        private long deliveryCount;

        public PendingMessageInfo() {
        }

        public PendingMessageInfo(String id, String consumerName, long idleTimeMs, long deliveryCount) {
            this.id = id;
            this.consumerName = consumerName;
            this.idleTimeMs = idleTimeMs;
            this.deliveryCount = deliveryCount;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConsumerName() {
            return consumerName;
        }

        public void setConsumerName(String consumerName) {
            this.consumerName = consumerName;
        }

        public long getIdleTimeMs() {
            return idleTimeMs;
        }

        public void setIdleTimeMs(long idleTimeMs) {
            this.idleTimeMs = idleTimeMs;
        }

        public long getDeliveryCount() {
            return deliveryCount;
        }

        public void setDeliveryCount(long deliveryCount) {
            this.deliveryCount = deliveryCount;
        }
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public long getQueueDepth() {
        return queueDepth;
    }

    public void setQueueDepth(long queueDepth) {
        this.queueDepth = queueDepth;
    }

    public long getPendingMessages() {
        return pendingMessages;
    }

    public void setPendingMessages(long pendingMessages) {
        this.pendingMessages = pendingMessages;
    }

    public long getDlqSize() {
        return dlqSize;
    }

    public void setDlqSize(long dlqSize) {
        this.dlqSize = dlqSize;
    }

    public long getDelayedQueueSize() {
        return delayedQueueSize;
    }

    public void setDelayedQueueSize(long delayedQueueSize) {
        this.delayedQueueSize = delayedQueueSize;
    }

    public List<ConsumerGroupInfo> getConsumerGroups() {
        return consumerGroups;
    }

    public void setConsumerGroups(List<ConsumerGroupInfo> consumerGroups) {
        this.consumerGroups = consumerGroups;
    }

    public List<PendingMessageInfo> getRecentPending() {
        return recentPending;
    }

    public void setRecentPending(List<PendingMessageInfo> recentPending) {
        this.recentPending = recentPending;
    }

    public double getThroughputPerMinute() {
        return throughputPerMinute;
    }

    public void setThroughputPerMinute(double throughputPerMinute) {
        this.throughputPerMinute = throughputPerMinute;
    }

    public double getEstimatedLatencySeconds() {
        return estimatedLatencySeconds;
    }

    public void setEstimatedLatencySeconds(double estimatedLatencySeconds) {
        this.estimatedLatencySeconds = estimatedLatencySeconds;
    }
}
