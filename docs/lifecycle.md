# Distributed Job Processing System — Job Lifecycle & Failure Mechanics

This document explains the state transitions, retry policies, exponential backoff calculations, Dead Letter Queue (DLQ) operations, and orphan recovery procedures.

---

## 1. Job State Transition Diagram

```
                 +-----------------------+
                 |       SUBMITTED       |
                 +-----------+-----------+
                             |
                             v
                 +-----------------------+
   +------------>|        QUEUED         |--------------+
   |             +-----------+-----------+              |
   |                         |                          |
   |                         | Worker picks up          | Cancel API
   |                         v                          |
   |             +-----------------------+              |
   |             |        RUNNING        |              |
   |             +-----+-----------+-----+              |
   |                   |           |                    |
   |   Success         |           | Failure            |
   |   Execution       |           |                    |
   |                   v           v                    |
   |             +-----------+   +-----------+          |
   |             | COMPLETED |   | RETRYING  |          |
   |             +-----------+   +-----+-----+          |
   |                                   |                |
   |   Delay Elapsed (Backoff ZSet)    | Max Retries    |
   +-----------------------------------+ Exceeded       |
                                       |                |
                                       v                v
                                 +-----------+    +-----------+
                                 |  FAILED   |    | CANCELLED |
                                 |  -> DLQ   |    +-----------+
                                 +-----+-----+
                                       |
                                       | Manual Retry API
                                       +--------------------> [QUEUED]
```

---

## 2. State Definitions

| State | Description | Allowed Next States |
|---|---|---|
| `QUEUED` | The job has been enqueued into Redis Stream `job:stream` and is awaiting an idle worker thread. | `RUNNING`, `CANCELLED` |
| `RUNNING` | A worker consumer has claimed the job and is actively executing its payload. | `COMPLETED`, `RETRYING`, `FAILED`, `CANCELLED` |
| `COMPLETED` | Execution completed successfully. Output result saved in MongoDB. Message acknowledged via `XACK`. | Terminal State |
| `RETRYING` | Execution failed or timed out, but attempts < `maxRetries`. Job scheduled in Redis delayed ZSET with exponential backoff. | `QUEUED` (when delay elapses) |
| `FAILED` | All retry attempts exhausted. Terminal failure. Record transferred to Dead Letter Queue (`job:stream:dlq`). | `QUEUED` (via manual retry API) |
| `CANCELLED` | Job execution was explicitly aborted by user request. | `QUEUED` (via manual retry API) |

---

## 3. Exponential Backoff Policy

When a job fails during execution, the system calculates an exponential backoff delay before re-enqueuing:

$$\text{Delay}(n) = \text{BaseDelayMs} \times \text{Multiplier}^{(n - 1)}$$

### Default Configuration:
- `BaseDelayMs` = 2,000 ms (2.0s)
- `Multiplier` = 2.0
- `MaxRetries` = 3

### Progression Schedule:
- **Attempt 1 Failure**: Delay = $2000 \times 2.0^0 = 2,000\text{ ms}$ (2s)
- **Attempt 2 Failure**: Delay = $2000 \times 2.0^1 = 4,000\text{ ms}$ (4s)
- **Attempt 3 Failure**: Retries exhausted $\rightarrow$ Status transitions to `FAILED`, record moved to `job:stream:dlq`.

---

## 4. Dead Letter Queue (DLQ) Architecture

The Dead Letter Queue operates on a separate Redis Stream key:
`job:stream:dlq`

### DLQ Message Payload Structure
- `jobId`: Original job identifier.
- `name`: Human-readable task name.
- `type`: Job type (`EMAIL`, `REPORT`, etc.).
- `attemptCount`: Number of attempts executed prior to failure.
- `reason`: Exception message and error classification.
- `failedAt`: ISO-8601 timestamp of final failure.

Jobs in the DLQ can be audited via the dashboard Queue Monitoring page and re-submitted to the active queue using the manual Retry action.

---

## 5. Execution Watchdog & Timeout

To prevent indefinite resource blocking from stalled network sockets or infinite loops:
- Each job defines a `timeoutSeconds` attribute (default: 60 seconds).
- The worker executes the task via `CompletableFuture.supplyAsync()` and monitors completion with `future.get(timeoutSeconds, TimeUnit.SECONDS)`.
- If the deadline expires, the worker triggers an immediate `TimeoutException`, registers the error, increments the attempt counter, and schedules a retry or sends to DLQ.

---

## 6. Worker Failure & Orphan Recovery

If an entire worker node crashes (e.g. OOM, host shutdown, or network partition):
1. **Heartbeat Loss**: The `WorkerRecoveryScheduler` scans the `workers` MongoDB collection every 10 seconds.
2. **Dead Node Detection**: Workers whose last heartbeat is older than `dead-worker-threshold-seconds` (30s) are marked `DEAD`.
3. **Orphan Re-queueing**: Any job marked `RUNNING` that was claimed by a dead worker or whose started timestamp exceeds the execution timeout is automatically reset to `QUEUED` and re-appended to `job:stream`.
