# Distributed Job Processing System — REST API Specification

Base URL: `/api/v1`
Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON Schema: `http://localhost:8080/v3/api-docs`

---

## 1. Submit Job
**`POST /api/v1/jobs`**

Creates a new asynchronous job and enqueues it into Redis Streams.

### Headers
| Header | Type | Description |
|---|---|---|
| `Idempotency-Key` | String | *(Optional)* Unique key to prevent duplicate job execution |

### Request Body
```json
{
  "name": "Quarterly Financial Analysis",
  "type": "REPORT",
  "priority": "HIGH",
  "payload": {
    "reportName": "Q3 Revenue Report",
    "format": "PDF",
    "metrics": ["ebitda", "cac", "churn"],
    "simulateFailure": false
  },
  "maxRetries": 3,
  "timeoutSeconds": 60,
  "idempotencyKey": "idem_q3_report_001"
}
```

### Response `201 Created`
```json
{
  "success": true,
  "message": "Job submitted successfully",
  "data": {
    "id": "job_a1b2c3d4e5f6",
    "name": "Quarterly Financial Analysis",
    "type": "REPORT",
    "priority": "HIGH",
    "status": "QUEUED",
    "payload": { ... },
    "attemptCount": 0,
    "maxRetries": 3,
    "timeoutSeconds": 60,
    "createdAt": "2026-11-03T12:00:00Z"
  },
  "timestamp": "2026-11-03T12:00:00.123Z"
}
```

---

## 2. List Jobs
**`GET /api/v1/jobs`**

Query jobs with pagination, filtering, and sorting.

### Query Parameters
| Parameter | Type | Default | Description |
|---|---|---|---|
| `status` | String | - | Filter by `QUEUED`, `RUNNING`, `COMPLETED`, `FAILED`, `RETRYING`, `CANCELLED` |
| `type` | String | - | Filter by `EMAIL`, `REPORT`, `DATA_PROCESSING`, `CUSTOM` |
| `priority` | String | - | Filter by `LOW`, `NORMAL`, `HIGH`, `CRITICAL` |
| `search` | String | - | Case-insensitive regex match on ID or Name |
| `startDate` | ISO-8601 | - | Filter jobs created on or after this timestamp |
| `endDate` | ISO-8601 | - | Filter jobs created on or before this timestamp |
| `page` | Integer | `0` | Zero-indexed page number |
| `size` | Integer | `10` | Page size limit |
| `sortBy` | String | `createdAt` | Property to sort by (`createdAt`, `durationMs`, etc.) |
| `sortDir` | String | `DESC` | Sort direction (`ASC` or `DESC`) |

### Response `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "job_a1b2c3d4e5f6",
        "name": "Quarterly Financial Analysis",
        "type": "REPORT",
        "priority": "HIGH",
        "status": "COMPLETED",
        "durationMs": 950,
        "attemptCount": 1,
        "workerId": "worker-node-1",
        "createdAt": "2026-11-03T12:00:00Z",
        "completedAt": "2026-11-03T12:00:01Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5,
    "last": false
  }
}
```

---

## 3. Get Job Details
**`GET /api/v1/jobs/{id}`**

Retrieve complete metadata, input payload, output result, and error diagnostics for a specific job.

### Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "job_a1b2c3d4e5f6",
    "name": "Quarterly Financial Analysis",
    "type": "REPORT",
    "priority": "HIGH",
    "status": "COMPLETED",
    "payload": { "format": "PDF" },
    "result": {
      "downloadUrl": "https://storage.internal/reports/rpt_98124.pdf",
      "rowCount": 14250,
      "fileSizeKb": 845
    },
    "attemptCount": 1,
    "maxRetries": 3,
    "durationMs": 950,
    "workerId": "worker-node-1",
    "createdAt": "2026-11-03T12:00:00Z",
    "startedAt": "2026-11-03T12:00:00.050Z",
    "completedAt": "2026-11-03T12:00:01.000Z"
  }
}
```

---

## 4. Get Job Logs
**`GET /api/v1/jobs/{id}/logs`**

Returns chronological structured logs emitted during job processing.

### Response `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "log_1",
      "jobId": "job_a1b2c3d4e5f6",
      "timestamp": "2026-11-03T12:00:00.100Z",
      "level": "INFO",
      "message": "Job submitted with status QUEUED"
    },
    {
      "id": "log_2",
      "jobId": "job_a1b2c3d4e5f6",
      "timestamp": "2026-11-03T12:00:00.250Z",
      "level": "INFO",
      "message": "Picked up by worker [worker-node-1] on attempt 1/3"
    },
    {
      "id": "log_3",
      "jobId": "job_a1b2c3d4e5f6",
      "timestamp": "2026-11-03T12:00:01.000Z",
      "level": "INFO",
      "message": "Job COMPLETED successfully in 950 ms"
    }
  ]
}
```

---

## 5. Retry Job
**`POST /api/v1/jobs/{id}/retry`**

Resets a `FAILED` or `CANCELLED` job and places it back into the active queue.

---

## 6. Cancel Job
**`POST /api/v1/jobs/{id}/cancel`**

Cancels an active or queued job. Rejects cancellation if already `COMPLETED`.

---

## 7. Job Statistics
**`GET /api/v1/jobs/stats`**

High-level summary KPIs (total, queued, running, completed, failed, success rate %, avg latency).

---

## 8. Worker Fleet Telemetry
**`GET /api/v1/workers`**

Lists all active worker nodes, concurrency, CPU, memory, active tasks, and heartbeats.

---

## 9. Queue Telemetry
**`GET /api/v1/queue/stats`**

Returns Redis Stream backlog depth, consumer group PEL pending count, DLQ size, and delayed queue counts.

---

## 10. Performance Analytics
**`GET /api/v1/analytics`**

Provides time-series throughput trends, latency percentiles (p50, p90, p99), job type distributions, and failure root causes.

---

## 11. System Settings
- `GET /api/v1/settings`: Read configurable parameters (retries, timeouts, backoff).
- `PUT /api/v1/settings`: Persist updated engine configuration.
