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
