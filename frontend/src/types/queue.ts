export interface ConsumerGroupInfo {
  name: string;
  consumers: number;
  pending: number;
  lastDeliveredId: string;
}

export interface PendingMessageInfo {
  id: string;
  consumerName: string;
  idleTimeMs: number;
  deliveryCount: number;
}

export interface QueueStats {
  streamName: string;
  queueDepth: number;
  pendingMessages: number;
  dlqSize: number;
  delayedQueueSize: number;
  consumerGroups: ConsumerGroupInfo[];
  recentPending: PendingMessageInfo[];
  throughputPerMinute: number;
  estimatedLatencySeconds: number;
}
