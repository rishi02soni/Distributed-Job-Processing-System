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


