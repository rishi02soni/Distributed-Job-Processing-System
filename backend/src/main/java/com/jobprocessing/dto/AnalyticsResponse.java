package com.jobprocessing.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {

    private List<ThroughputPoint> throughputOverTime;
    private LatencyMetrics latency;
    private Map<String, Long> jobTypeDistribution;
    private Map<String, Long> failureReasonDistribution;
    private List<WorkerPerformance> workerPerformance;
    private List<QueueGrowthPoint> queueGrowth;

    public AnalyticsResponse() {
    }

    public static class ThroughputPoint {
        private String timestamp;
        private long completed;
        private long failed;

        public ThroughputPoint() {}

        public ThroughputPoint(String timestamp, long completed, long failed) {
            this.timestamp = timestamp;
            this.completed = completed;
            this.failed = failed;
        }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
        public long getFailed() { return failed; }
        public void setFailed(long failed) { this.failed = failed; }
    }

    public static class LatencyMetrics {
        private double p50;
        private double p90;
        private double p99;
        private double avg;
        private double min;
        private double max;

        public LatencyMetrics() {}

        public LatencyMetrics(double p50, double p90, double p99, double avg, double min, double max) {
            this.p50 = p50;
            this.p90 = p90;
            this.p99 = p99;
            this.avg = avg;
            this.min = min;
            this.max = max;
        }

        public double getP50() { return p50; }
        public void setP50(double p50) { this.p50 = p50; }
        public double getP90() { return p90; }
        public void setP90(double p90) { this.p90 = p90; }
        public double getP99() { return p99; }
        public void setP99(double p99) { this.p99 = p99; }
        public double getAvg() { return avg; }
        public void setAvg(double avg) { this.avg = avg; }
        public double getMin() { return min; }
        public void setMin(double min) { this.min = min; }
        public double getMax() { return max; }
        public void setMax(double max) { this.max = max; }
    }

    public static class WorkerPerformance {
        private String workerId;
        private long jobsCompleted;
        private long jobsFailed;
        private double avgDurationMs;
        private double successRate;

        public WorkerPerformance() {}

        public WorkerPerformance(String workerId, long jobsCompleted, long jobsFailed, double avgDurationMs, double successRate) {
            this.workerId = workerId;
            this.jobsCompleted = jobsCompleted;
            this.jobsFailed = jobsFailed;
            this.avgDurationMs = avgDurationMs;
            this.successRate = successRate;
        }

        public String getWorkerId() { return workerId; }
        public void setWorkerId(String workerId) { this.workerId = workerId; }
        public long getJobsCompleted() { return jobsCompleted; }
        public void setJobsCompleted(long jobsCompleted) { this.jobsCompleted = jobsCompleted; }
        public long getJobsFailed() { return jobsFailed; }
        public void setJobsFailed(long jobsFailed) { this.jobsFailed = jobsFailed; }
        public double getAvgDurationMs() { return avgDurationMs; }
        public void setAvgDurationMs(double avgDurationMs) { this.avgDurationMs = avgDurationMs; }
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
    }

    public static class QueueGrowthPoint {
        private String time;
        private long depth;

        public QueueGrowthPoint() {}

        public QueueGrowthPoint(String time, long depth) {
            this.time = time;
            this.depth = depth;
        }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public long getDepth() { return depth; }
        public void setDepth(long depth) { this.depth = depth; }
    }

    public List<ThroughputPoint> getThroughputOverTime() { return throughputOverTime; }
    public void setThroughputOverTime(List<ThroughputPoint> throughputOverTime) { this.throughputOverTime = throughputOverTime; }
    public LatencyMetrics getLatency() { return latency; }
    public void setLatency(LatencyMetrics latency) { this.latency = latency; }
    public Map<String, Long> getJobTypeDistribution() { return jobTypeDistribution; }
    public void setJobTypeDistribution(Map<String, Long> jobTypeDistribution) { this.jobTypeDistribution = jobTypeDistribution; }
    public Map<String, Long> getFailureReasonDistribution() { return failureReasonDistribution; }
    public void setFailureReasonDistribution(Map<String, Long> failureReasonDistribution) { this.failureReasonDistribution = failureReasonDistribution; }
    public List<WorkerPerformance> getWorkerPerformance() { return workerPerformance; }
    public void setWorkerPerformance(List<WorkerPerformance> workerPerformance) { this.workerPerformance = workerPerformance; }
    public List<QueueGrowthPoint> getQueueGrowth() { return queueGrowth; }
    public void setQueueGrowth(List<QueueGrowthPoint> queueGrowth) { this.queueGrowth = queueGrowth; }
}
