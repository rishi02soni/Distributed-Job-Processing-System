# 🚀 Distributed Job Processing System

<p align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-Microservices-brightgreen)
![Redis](https://img.shields.io/badge/Redis-Distributed%20Queue-red)
![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-green)
![Docker](https://img.shields.io/badge/Docker-Container-blue)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestration-blueviolet)
![License](https://img.shields.io/badge/License-MIT-yellow)

</p>

---

<p align="center">
<img src="https://media.giphy.com/media/xT0xeJpnrWC4XWblEk/giphy.gif" width="500">
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

# 📌 Problem Statement

Modern applications often need to process **long-running background tasks** such as:

- Email notifications  
- Report generation  
- Image/video processing  
- Data synchronization  
- Payment reconciliation  

If these tasks are executed **synchronously**, they block user requests and degrade system performance.

This project solves the problem by implementing **asynchronous distributed job execution using Redis queues and worker services**.

---

# 🏗 System Architecture

<p align="center">
<img src="https://integration-developer.de/en/wp-content/uploads/2024/10/diagram-export-10-2-2024-2_10_18-PM.png" width="900"/>
</p>

### High Level Flow
Client Request
↓
API Service (Spring Boot)
↓
Redis Job Queue
↓
Worker Microservices
↓
MongoDB Job Storage



### Processing Flow

1. Client sends a job request to the API
2. API service creates a job record
3. Job is pushed to the **Redis Queue**
4. Worker services continuously consume jobs
5. Worker processes the task asynchronously
6. Job results and status are stored in **MongoDB**
7. Client can check job status using API

---

# ⚙️ Tech Stack

| Layer | Technology |
|------|------------|
Backend | Java, Spring Boot |
Queue System | Redis |
Database | MongoDB |
Containerization | Docker |
Orchestration | Kubernetes |
Monitoring | Prometheus + Grafana |
Logging | Structured Logs / ELK |
API | RESTful APIs |

---

# 🧠 Core Features

### ⚡ Asynchronous Job Processing
Jobs are executed asynchronously using a **Redis-backed queue system**.

### 📦 Distributed Workers
Multiple worker services consume jobs concurrently to improve throughput.

### 🔁 Retry Mechanism
Failed jobs automatically retry using configurable retry policies.

### 🧱 Fault Tolerance
Worker failures do not cause job loss due to queue persistence.

### 📊 Observability
System metrics and logs provide visibility into job processing.

### 📈 Horizontal Scaling
Worker services scale automatically using **Kubernetes autoscaling**.

---

# 📂 Project Structure
## 📁 Project Structure

```
distributed-job-system
│
├── api-service
│   │
│   ├── controller        # REST APIs to submit and manage jobs
│   ├── service           # Business logic layer
│   ├── repository        # Database interaction layer
│   └── config            # Application configuration
│
├── worker-service
│   │
│   ├── job-consumer      # Consumes jobs from Redis queue
│   ├── job-handlers      # Processes different types of jobs
│   └── scheduler         # Handles scheduled/background tasks
│
├── common
│   │
│   ├── dto               # Data Transfer Objects
│   ├── models            # Shared data models
│   └── utils             # Utility/helper classes
│
└── infrastructure
    │
    ├── docker            # Dockerfiles & container configs
    └── kubernetes        # Kubernetes deployment manifests
```

---

# 📡 API Endpoints

### Create Job
POST /api/jobs

Example Request

```json
{
  "type": "email",
  "payload": {
    "email": "user@example.com",
    "message": "Welcome to our platform"
  }
}

```
Get Job Status

GET /api/jobs/{jobId}

Example Response
{
  "id": "123",
  "type": "email",
  "status": "COMPLETED",
  "createdAt": "timestamp"
}

```
🗃 Job Data Model
{
  "id": "jobId",
  "type": "email",
  "status": "QUEUED",
  "payload": {},
  "result": {},
  "retryCount": 0,
  "createdAt": "timestamp",
  "completedAt": "timestamp"
}
```
Job Status Types
QUEUED
PROCESSING
COMPLETED
FAILED
RETRYING

## 🔁 Job Processing Flow

<p align="center">
  <img src="https://media.giphy.com/media/26BRuo6sLetdllPAQ/giphy.gif" width="450">
</p>

### 📌 How the System Works

1️⃣ **Client sends job request**
→ A user or application sends a job request to the **API Service** via REST endpoint.

2️⃣ **API validates & creates job**
→ The API validates input, generates a **Job ID**, and stores initial metadata.

3️⃣ **Job pushed to Redis Queue**
→ The job is published to the **Redis queue** for asynchronous processing.

4️⃣ **Worker Service consumes job**
→ Worker services continuously listen (poll/subscribe) to the Redis queue.

5️⃣ **Job processed asynchronously**
→ The worker executes the job using appropriate **job handlers**.

6️⃣ **Result stored in MongoDB**
→ Job results, status (`SUCCESS`, `FAILED`, `PROCESSING`) are stored in **MongoDB**.

7️⃣ **Client checks job status**
→ Clients query the API using the **Job ID** to retrieve the job result/status.

---

### ⚡ Flow Summary

```
Client
  │
  ▼
API Service
  │
  ▼
Redis Queue
  │
  ▼
Worker Service
  │
  ▼
Job Handler
  │
  ▼
MongoDB
  │
  ▼
Client (Fetch Job Status)
```
## 🛡 Fault Tolerance Strategy

### 🔁 Retry Policy

* If `retryCount < 3`
  → The system retries the job automatically.

* If retry attempts exceed the limit
  → The job is moved to the **Dead Letter Queue (DLQ)** for further inspection or manual recovery.

---

## 📊 Observability

Monitoring and logging are implemented using modern observability tools:

* **Prometheus** → Collects system and application metrics
* **Grafana** → Provides monitoring dashboards and visualization
* **Structured Logging** → Enables easier debugging and log analysis

### Example Metrics

```
jobs_processed_total
jobs_failed_total
queue_length
worker_active_count
```

---

## 🐳 Docker Deployment

Build and run all services using Docker Compose:

```bash
docker-compose up --build
```

### Services Started

* API Service
* Worker Service
* Redis
* MongoDB
* Prometheus
* Grafana

---

## ☸ Kubernetes Deployment

Deploy all services to Kubernetes:

```bash
kubectl apply -f k8s/
```

This deploys the system using Kubernetes manifests defined in the **k8s/** directory.

---

## 📈 Scaling Strategy

```
More Jobs → Queue Length Increases
        ↓
Kubernetes Auto-Scales Worker Pods
        ↓
More Workers Process Jobs Faster
```

This enables **horizontal scalability** and efficient job processing during high load.

---

## 🚀 Future Improvements

* Priority-based job scheduling
* Delayed job execution
* Rate-limited job processing
* WebSocket-based live job updates
* Distributed tracing using **OpenTelemetry**
* Event-driven architecture with **Kafka**

---

## 🧪 Running Locally

### 1️⃣ Clone Repository

```bash
git clone https://github.com/yourusername/distributed-job-system
cd distributed-job-system
```

### 2️⃣ Start Infrastructure

```bash
docker-compose up
```

### 3️⃣ Run Services

```bash
mvn spring-boot:run
```

---

## 📚 Key Distributed System Concepts Demonstrated

* Queue-based system design
* Asynchronous job processing
* Horizontal scalability
* Microservices architecture
* Fault tolerance mechanisms
* Observability and monitoring

---

## 🤝 Contributing

Pull requests are welcome.

For major changes, please open an issue first to discuss what you would like to change.

---

## ⭐ If You Like This Project

Give it a **star ⭐ on GitHub** and help others discover it!
