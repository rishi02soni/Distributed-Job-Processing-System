# 🚀 Distributed Job Processing System

<p align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-Backend-brightgreen)
![Redis](https://img.shields.io/badge/Redis-Queue-red)
![MongoDB](https://img.shields.io/badge/MongoDB-Database-green)
![Docker](https://img.shields.io/badge/Docker-Container-blue)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestration-blueviolet)
![License](https://img.shields.io/badge/License-MIT-yellow)

</p>

---

<p align="center">
  <img src="https://media.giphy.com/media/LmNwrBhejkK9EFP504/giphy.gif" width="500">
</p>

A **cloud-native distributed backend system** designed to process asynchronous background jobs at scale.

The system decouples **job submission from execution** using a **Redis-powered distributed queue**, enabling scalable and fault-tolerant job processing with horizontally scalable worker services.

Built using **Java, Spring Boot, Redis, MongoDB, Docker, and Kubernetes**, this project demonstrates production-level **distributed system design patterns** such as:

- Asynchronous job processing
- Queue-based architecture
- Worker orchestration
- Fault tolerance & retries
- Observability and monitoring

---

# 📌 Problem Statement

Modern applications often need to process **long-running background tasks** such as:

- Email notifications
- Report generation
- Image/video processing
- Data synchronization
- Payment reconciliation

Processing these tasks synchronously blocks user requests and reduces system performance.

This system solves the problem by implementing **asynchronous distributed job execution**.

---

# 🏗 System Architecture

<p align="center">
<img src="https://media.giphy.com/media/26BRuo6sLetdllPAQ/giphy.gif" width="450">
</p>
Client Request
↓
API Service (Spring Boot)
↓
Redis Job Queue
↓
Worker Microservices
↓
MongoDB Job Storage

### Flow

1. Client sends a job request
2. API service creates a job
3. Job is pushed into Redis queue
4. Worker services consume jobs
5. Results stored in MongoDB
6. Client can track job status

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
Logging | ELK Stack / Structured Logs |
API | RESTful APIs |

---

# 🧠 Core Features

### ⚡ Asynchronous Job Processing
Jobs are executed asynchronously using Redis queueing.

### 📦 Distributed Workers
Multiple worker services process jobs concurrently.

### 🔁 Retry Mechanism
Failed jobs automatically retry using configurable retry policies.

### 🧱 Fault Tolerance
Worker failures do not lose jobs due to queue persistence.

### 📊 Observability
Integrated monitoring using metrics and structured logs.

### 📈 Horizontal Scaling
Worker services scale automatically using Kubernetes.

---

# 📂 Project Structure
distributed-job-system

api-service
│
├── controller
├── service
├── repository
└── config

worker-service
│
├── job-consumer
├── job-handlers
└── scheduler

common
│
├── dto
├── models
└── utils

infrastructure
│
├── docker
└── kubernetes

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
Get Job Status

GET /api/jobs/{jobId}

Response
{
"id": "123",
"type": "email",
"status": "COMPLETED",
"createdAt": "timestamp"
}
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
Job Status Types:

QUEUED
PROCESSING
COMPLETED
FAILED
RETRYING

🔁 Job Processing Flow
<p align="center"> <img src="https://media.giphy.com/media/xT0xeJpnrWC4XWblEk/giphy.gif" width="450"> </p>

1️⃣ API receives job request
2️⃣ Job pushed to Redis queue
3️⃣ Worker service polls queue
4️⃣ Job processed asynchronously
5️⃣ Result stored in MongoDB
6️⃣ Client checks job status

🛡 Fault Tolerance Strategy
Retry Mechanism

retryCount < 3
→ retry job

If retries exceed limit:

move to Dead Letter Queue
📊 Observability

Monitoring implemented with:

Prometheus (Metrics)

Grafana (Dashboards)

Structured Logging

Example metrics:

jobs_processed_total
jobs_failed_total
queue_length
worker_active_count

🐳 Docker Deployment

Build and run containers:

docker-compose up --build
Services started:

API Service

Worker Service

Redis

MongoDB

Prometheus

Grafana
☸ Kubernetes Deployment

Deploy using:

kubectl apply -f k8s/

📈 Scaling Strategy

System supports horizontal scaling:

More Jobs → Queue Length Increases
↓
Kubernetes Auto Scales Workers
↓
More Workers Process Jobs

More Jobs → Queue Length Increases
↓
Kubernetes Auto Scales Workers
↓
More Workers Process Jobs

🚀 Future Improvements

Priority-based job scheduling

Delayed job execution

Rate-limited task processing

WebSocket live job updates

Distributed tracing (OpenTelemetry)

Event-driven architecture using Kafka

🧪 Running Locally
Clone Repository

git clone https://github.com/yourusername/distributed-job-system
Start Infrastructure

docker-compose up

Run Services

mvn spring-boot:run

📚 Key Distributed System Concepts Demonstrated

Queue-based system design

Asynchronous processing

Horizontal scalability

Microservices architecture

Fault tolerance

Observability

🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
