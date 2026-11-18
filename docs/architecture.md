# Distributed Job Processing System — Architecture

This document provides a detailed overview of the system architecture, reliability mechanics, storage schemas, and distributed coordination patterns used throughout the Distributed Job Processing System.

---

## 1. High-Level Architecture Overview

```
                      +------------------------------------------+
                      |         Developer Web Dashboard          |
                      |  (React 18, Vite, TanStack Query, UI)    |
                      +--------------------+---------------------+
                                           | HTTP / REST (OpenAPI 3)
                                           v
                      +------------------------------------------+
                      |     Spring Boot REST API Gateway         |
                      |  - Job validation & submission           |
                      |  - Idempotency guard & deduplication     |
                      |  - Pagination, sorting, query filtering  |
                      |  - Micrometer & Actuator observability   |
                      +-------------+--------------------+-------+
                                    |                    |
                          Reads &   | Writes             | XADD
                          Updates   v                    v
                      +-------------+-------+    +-------+------------------+
                      |     MongoDB 7       |    |      Redis Streams       |
                      | - jobs              |    | - job:stream (Main)      |
                      | - job_logs          |    | - job:stream:dlq (DLQ)   |
                      | - workers           |    | - job:delayed (ZSet)     |
                      | - system_settings   |    +-------+------------------+
                      +---------------------+            |
                                                         | XREADGROUP / XCLAIM
                                                         v
                                           +-------------+------------------+
                                           |    Worker Nodes Cluster        |
                                           | - Consumer Group 'worker-group'|
                                           | - Concurrency Thread Pool      |
                                           | - Execution Watchdog & Timeout |
                                           | - Heartbeat & Recovery System  |
                                           | - Safe ACK & DLQ Dispatch      |
                                           +--------------------------------+
```

---

## 2. Distributed Queue Engine: Redis Streams

### Stream Keys & Structures
- **`job:stream` (Main Stream)**:
  - Jobs are appended via `XADD job:stream MAXLEN ~ 10000 * jobId ... type ... priority ...`.
  - Stream retains ordering, timestamps, and message offsets.
- **Consumer Group `worker-group`**:
  - Workers join as consumers (`worker-<hostname>-<uuid>`).
  - Read via `XREADGROUP GROUP worker-group <workerId> COUNT <n> BLOCK 2000 STREAMS job:stream >`.
  - Each message read is placed in the **Pending Entries List (PEL)** until explicitly acknowledged.
- **Safe Acknowledgment (`XACK`)**:
  - `XACK job:stream worker-group <recordId>` is **only** invoked after the worker has processed the job and updated the job state in MongoDB.
  - If a worker crashes before invoking `XACK`, the message remains in the PEL and can be claimed by other workers.
- **`job:delayed` (Scheduled & Retry Sorted Set)**:
  - Jobs scheduled for the future or retrying with exponential backoff are stored in a Redis Sorted Set (ZSET).
  - Score = Target execution timestamp (`System.currentTimeMillis() + delayMs`).
  - A dedicated `DelayedQueueScheduler` polls `ZRANGEBYSCORE job:delayed 0 <now>` every 1,000ms and re-enqueues ready jobs back to `job:stream`.
- **`job:stream:dlq` (Dead Letter Queue)**:
  - When a job exceeds its configured `maxRetries` (default: 3), it is transferred to the Dead Letter Queue.
  - DLQ records include failure timestamp, error type, and failure reason for post-mortem auditing.

---

## 3. Worker Node Architecture

Each worker instance is an autonomous, non-blocking component:
1. **Concurrency Pool**: Managed by a bounded `ExecutorService` configured via `app.worker.concurrency` (default: 4 threads).
2. **Watchdog Execution**:
   - Each job execution is wrapped in a `CompletableFuture` governed by `job.getTimeoutSeconds()` (default: 60s).
   - If execution exceeds this window, a `TimeoutException` is raised, releasing the thread and triggering the retry/failure flow.
3. **Heartbeat Scheduler**:
   - Runs every 5 seconds (`@Scheduled(fixedRate = 5000)`).
   - Captures JVM CPU load and memory usage via `OperatingSystemMXBean` and `Runtime.getRuntime()`.
   - Persists node health into MongoDB collection `workers`.
4. **Dead Worker Eviction & Orphan Recovery**:
   - Stale workers (no heartbeat for > 30s) are marked as `DEAD`.
   - Any job marked `RUNNING` under a dead worker or exceeding its maximum timeout is automatically reclaimed and re-queued.

---

## 4. Delivery Guarantees & Race Condition Prevention

### Delivery Guarantee: At-Least-Once with Idempotency
- **Deduplication**: Job submissions support an `Idempotency-Key` header or payload field. A sparse unique index on `jobs.idempotencyKey` prevents duplicate job creation across simultaneous network retries.
- **Optimistic Locking**: The `Job` document utilizes Spring Data's `@Version` field to prevent conflicting concurrent writes between worker completion, cancellation, and retry commands.
- **Terminal State Guard**: If a worker consumes a message for a job that was already marked `COMPLETED` or `CANCELLED`, the worker immediately executes `XACK` and aborts processing.

---

## 5. MongoDB Data Model

### Collection: `jobs`
| Field | Type | Description |
|---|---|---|
| `id` | String | Prefixed unique identifier (`job_xxxxxx`) |
| `name` | String | User-provided descriptor |
| `type` | Enum | `EMAIL`, `REPORT`, `DATA_PROCESSING`, `CUSTOM` |
| `priority` | Enum | `LOW`, `NORMAL`, `HIGH`, `CRITICAL` |
| `status` | Enum | `QUEUED`, `RUNNING`, `COMPLETED`, `FAILED`, `RETRYING`, `CANCELLED` |
| `payload` | Document | JSON task parameters |
| `result` | Document | Execution output produced by worker |
| `errorDetails`| Document | Stack trace, message, and error class |
| `attemptCount`| Integer | Current execution attempt number |
| `maxRetries` | Integer | Allowed retries before DLQ routing |
| `timeoutSeconds` | Integer | Execution deadline |
| `idempotencyKey`| String | Indexed unique constraint |
| `workerId` | String | Node currently or most recently processing the job |
| `durationMs` | Long | Total execution time in milliseconds |
| `createdAt` | Timestamp | Initial submission time |
| `startedAt` | Timestamp | Worker pickup time |
| `completedAt` | Timestamp | Final resolution time |
| `version` | Long | Optimistic locking revision |

### Collection: `job_logs`
Chronological execution logs associated with each job, indexed by `{ jobId: 1, timestamp: 1 }`.

### Collection: `workers`
Node registration records, capacity, concurrency, CPU/Memory telemetry, and heartbeat timestamps.
