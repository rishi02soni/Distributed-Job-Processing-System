# Distributed Job Processing System

[![CI Pipeline](https://github.com/engineering/distributed-job-processing-system/actions/workflows/ci.yml/badge.svg)](.github/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis Streams](https://img.shields.io/badge/Queue-Redis%20Streams-red.svg)](https://redis.io/)
[![MongoDB 7](https://img.shields.io/badge/Database-MongoDB%207-green.svg)](https://www.mongodb.com/)
[![React 18](https://img.shields.io/badge/Frontend-React%2018%20%2B%20TypeScript-blue.svg)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Styling-Tailwind%20CSS-38bdf8.svg)](https://tailwindcss.com/)

A production-grade, distributed asynchronous job processing platform where users submit jobs, track execution status in real-time, view structured logs, retry failed tasks, monitor worker fleet health, and analyze queue latency and processing performance through a dark-first developer web dashboard.

---

## 1. System Architecture

```
                   +-------------------------------------------------------+
                   |                 Developer Web Dashboard               |
                   |  (React 18, Vite, TypeScript, Tailwind, Recharts)     |
                   +---------------------------+---------------------------+
                                               | HTTP / REST (OpenAPI 3)
                                               v
                   +-------------------------------------------------------+
                   |             Spring Boot REST API Gateway              |
                   | - Job submission & validation (Idempotency Key)       |
                   | - State management & multi-criteria filtering         |
                   | - Prometheus metrics (/actuator/prometheus)           |
                   +-------------+---------------------------+-------------+
                                 |                           |
                       Reads &   | Writes                    | XADD
                       Updates   v                           v
                   +-------------+-------+     +-------------+-------------+
                   |     MongoDB 7       |     |        Redis Streams      |
                   | - jobs              |     | - job:stream (Main Queue) |
                   | - job_logs          |     | - job:stream:dlq (DLQ)    |
                   | - workers           |     | - job:delayed (ZSet)      |
                   | - system_settings   |     +-------------+-------------+
                   +---------------------+                   |
                                                             | XREADGROUP / XCLAIM
                                                             v
                                               +-------------+-------------+
                                               |     Worker Cluster        |
                                               | - Consumer Group Consumers|
                                               | - Concurrency Thread Pool |
                                               | - Heartbeat & Recovery    |
                                               | - Safe ACK & Idempotency  |
                                               +---------------------------+
```

---

## 2. Core Features

- **Distributed Queue with Redis Streams**: Consumer groups, atomic consumer assignment, Pending Entries List (PEL), and safe acknowledgment (`XACK`) after database commit.
- **Reliable Failure Handling**: Automatic exponential backoff retry scheduling using Redis Sorted Sets (`job:delayed`).
- **Dead Letter Queue (DLQ)**: Automatic isolation of persistently failing jobs into `job:stream:dlq` after exceeding retry limits.
- **Worker Concurrency & Watchdog**: Configurable thread concurrency per worker instance, execution timeout watchdogs, and heartbeats.
- **Auto-Recovery of Orphaned Jobs**: Periodic health checks evict unresponsive workers and safely claim stalled jobs back into the queue.
- **Idempotency Guarantees**: Prevent duplicate execution across retries using indexed idempotency keys.
- **Interactive Developer Dashboard**: 8 complete dark-mode pages built with React, Vite, Tailwind CSS, TanStack Query, and Recharts.
- **Full Observability**: Spring Boot Actuator, Prometheus metrics endpoint, and pre-configured Grafana dashboards.

---

## 3. Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21 LTS, Spring Boot 3.3.4, Spring Web, Spring Data MongoDB, Spring Data Redis, Spring Validation, Spring Actuator |
| **Database** | MongoDB 7.0 (Documents, Compound & Sparse Indexes, Optimistic Locking) |
| **Queue** | Redis 7.0 (Redis Streams, Consumer Groups, Sorted Sets, AOF Persistence) |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query v5, Recharts, Lucide Icons |
| **Testing** | JUnit 5, Mockito, WebMvcTest, AssertJ, Vitest, React Testing Library |
| **DevOps** | Docker, Docker Compose, Kubernetes manifests (Minikube-ready), GitHub Actions CI |
| **Monitoring** | Prometheus, Grafana, Micrometer metrics |

---

## 4. Repository Structure

```
distributed-job-processing-system/
├── backend/                             # Spring Boot 3 Core API & Worker Service
│   ├── src/main/java/com/jobprocessing/
│   │   ├── config/                     # Mongo, Redis, OpenAPI, Web MVC CORS
│   │   ├── controller/                 # REST Controllers (Jobs, Workers, Queue, Analytics, Settings)
│   │   ├── dto/                        # Request/Response Data Transfer Objects
│   │   ├── exception/                  # Global Exception Handler and error responses
│   │   ├── model/                      # MongoDB documents (Job, JobLog, WorkerNode, SystemSettings)
│   │   ├── repository/                 # Spring Data MongoDB Repositories
│   │   ├── service/                    # Business Logic Interfaces and Implementations
│   │   └── worker/                     # Redis Stream consumer loop, executors, schedulers
│   ├── src/test/java/                  # Unit, Mockito, and Controller Tests
│   ├── Dockerfile                      # Multi-stage JDK build -> Alpine JRE image
│   └── pom.xml                         # Maven dependencies and build configuration
├── frontend/                            # React 18 Developer Web Dashboard
│   ├── src/
│   │   ├── components/                 # Reusable UI (Button, Card, Badge, Input, Select) and Layout
│   │   ├── hooks/                      # TanStack Query custom data-fetching hooks
│   │   ├── pages/                      # 8 Production Dashboard Pages
│   │   ├── services/                   # Centralized API client (Fetch)
│   │   └── types/                      # TypeScript schemas
│   ├── Dockerfile                      # Node builder -> Nginx Alpine reverse proxy
│   ├── nginx.conf                      # Nginx SPA fallback and API proxy routing
│   └── package.json
├── infrastructure/
│   ├── docker/                         # Container assets
│   ├── kubernetes/                     # Minikube-ready Deployments, Services, ConfigMaps, Secrets, HPA
│   ├── prometheus/                     # prometheus.yml scrape configuration
│   └── grafana/                        # Datasource and Dashboard provisioning
├── docs/                               # Architecture, REST API, and Lifecycle specifications
├── docker-compose.yml                  # Local full-stack orchestration
├── .env.example                        # Configurable environment template
└── .github/workflows/ci.yml            # CI Pipeline for automated build and tests
```

---

## 5. Quickstart & Local Deployment

### Option A: Complete Local Stack via Docker Compose (Recommended)

Ensure Docker Desktop is running, then execute:

```bash
# 1. Clone or navigate to the repository
cd distributed-job-processing-system

# 2. Start all services in the background
docker-compose up -d --build

# 3. Check container health status
docker-compose ps
```

The stack will start the following services:
- **Frontend Dashboard**: [http://localhost:3000](http://localhost:3000)
- **Backend REST API**: [http://localhost:8080](http://localhost:8080)
- **Swagger OpenAPI Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Prometheus Metrics**: [http://localhost:9090](http://localhost:9090)
- **Grafana Dashboards**: [http://localhost:3001](http://localhost:3001) *(User: `admin`, Pass: `admin`)*
- **MongoDB**: `localhost:27017`
- **Redis**: `localhost:6379`

To view logs:
```bash
docker-compose logs -f backend
```

To stop the cluster:
```bash
docker-compose down
```

---

### Option B: Bare-Metal Local Development

#### Prerequisites
- Java 21+ JDK
- Maven 3.9+ (or included portable wrapper)
- Node.js 18+ and npm
- MongoDB 7.0 running on `localhost:27017`
- Redis 7.0 running on `localhost:6379`

#### 1. Start Backend API & Worker
```bash
cd backend
mvn clean spring-boot:run
```
Backend will start on port `8080`.

#### 2. Start Frontend Dev Server
```bash
cd frontend
npm install
npm run dev
```
Frontend will open on [http://localhost:3000](http://localhost:3000) with automatic proxying to the backend.

---

## 6. Running Tests

### Backend Unit & Integration Tests (JUnit 5 / Mockito)
```bash
cd backend
mvn clean test
```
*Executes all 16 test cases covering Job submission, idempotency deduplication, exponential backoff calculation, retry limit routing to DLQ, and REST controllers.*

### Frontend Component Tests (Vitest & React Testing Library)
```bash
cd frontend
npm run test
```

### Frontend Production Build Verification
```bash
cd frontend
npm run build
```

---

## 7. Kubernetes Deployment (Minikube)

Deploy the entire stack onto a local Minikube cluster:

```bash
# 1. Enable Minikube ingress
minikube addons enable ingress

# 2. Apply Namespace, ConfigMaps, and Secrets
kubectl apply -f infrastructure/kubernetes/namespace.yaml
kubectl apply -f infrastructure/kubernetes/configmap.yaml
kubectl apply -f infrastructure/kubernetes/secret.yaml

# 3. Deploy MongoDB and Redis
kubectl apply -f infrastructure/kubernetes/mongodb.yaml
kubectl apply -f infrastructure/kubernetes/redis.yaml

# 4. Deploy Backend API, Worker Cluster, and Frontend SPA
kubectl apply -f infrastructure/kubernetes/backend.yaml
kubectl apply -f infrastructure/kubernetes/worker.yaml
kubectl apply -f infrastructure/kubernetes/frontend.yaml

# 5. Verify Pods and HorizontalPodAutoscalers
kubectl get pods -n job-processing
kubectl get hpa -n job-processing
```

Access the frontend via NodePort:
```bash
minikube service frontend-service -n job-processing
```

---

## 8. Dashboard Overview & Pages

1. **Overview Dashboard (`/`)**: High-level KPI metrics (Total, Queued, Running, Completed, Failed, Success Rate %, Avg Duration, Queue Depth), real-time throughput Area chart, worker fleet health cards, and recent activity feed.
2. **Jobs Management (`/jobs`)**: Advanced data table with search, status pills, type filtering, date range filter, sortable columns, pagination, and one-click Retry/Cancel actions.
3. **Create Job (`/jobs/new`)**: Interactive job submission form with template presets for Email, Report, Data Processing, and Custom tasks, syntax-validating JSON editor, idempotency key generator, and schedule delay options.
4. **Job Details (`/jobs/:id`)**: Detailed inspection with state progress timeline, execution duration, payload and result JSON viewers, error stack traces, attempt history, and live terminal logs.
5. **Workers (`/workers`)**: Fleet monitoring showing active worker IDs, hostnames, CPU & Memory usage gauges, concurrency load bars, and heartbeat recency indicators.
6. **Queue Telemetry (`/queue`)**: Redis Streams inspection showing stream depth, consumer group member cursors, Pending Entries List (PEL) table with idle times, and Dead Letter Queue (DLQ) size.
7. **Analytics (`/analytics`)**: Deep performance insights including p50/p90/p99 latency percentiles bar charts, throughput over time, job type volume breakdown, and failure root cause classification.
8. **Settings (`/settings`)**: Engine configuration manager allowing runtime tuning of retry limits, base backoff ms, multipliers, watchdog timeouts, and thread concurrency.

---

## 9. Design Decisions & Trade-offs

- **Redis Streams over Kafka/RabbitMQ**: Redis Streams provides lightweight, in-memory low latency queuing with consumer groups, ACK tracking, and message persistence without the operational overhead of ZooKeeper/KRaft.
- **Redis Sorted Sets for Exponential Backoff**: Instead of blocking worker threads with `Thread.sleep()`, failing jobs are parked in a Redis Sorted Set with their future execution timestamp as the score. A lightweight poller awakens only ready jobs, keeping workers 100% productive.
- **At-Least-Once Delivery + Idempotency**: Redis Streams ensures messages are never lost during worker crashes via the Pending Entries List. Coupled with client-provided idempotency keys, duplicate execution is eliminated.
- **Separation of API Gateway and Worker Pods**: In Kubernetes, backend API deployments are scaled based on HTTP request volume, while worker deployments are scaled independently based on queue depth and CPU utilization.

---
phy.com/media/xT0xeJpnrWC4XWblEk/giphy.gif" width="500">
</p>

A **cloud-native distributed backend system** designed to process **asynchronous background jobs at scale**.

The system decouples **job submission from execution** using a **Redis-powered distributed queue**, enabling scalable and fault-tolerant job processing with horizontally scalable worker services.

Built using **Java, Spring Boot, Redis, MongoDB, Docker, and Kubernetes**, this project demonstrates production-level **distributed system design patterns** such as:

- Asynchronous job processing  
- Queue-based architecture  
- Distributed workers  
- Fault tolerance and retries  
- Observability and monitoring  
- Horizontal scalability  

---


## 10. License

Apache License 2.0. Open source for enterprise distributed processing and personal development.
