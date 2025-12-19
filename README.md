# Energy Management System (EMS) - Distributed Microservices

## Overview
This project is a comprehensive energy management solution developed using a **distributed microservices architecture**. The system enables real-time consumption monitoring, asynchronous processing of IoT sensor data, and intelligent customer support through AI integration.

---

## System Architecture
The system is built on **loose coupling** principles and is fully containerized using **Docker**.

| Component | Technology | Responsibility |
| :--- | :--- | :--- |
| **Frontend** | React.js | Role-based interface (Admin/Client) and consumption charts |
| **API Gateway** | Traefik | Reverse proxy, JWT validation, and request routing |
| **User Microservice** | Spring Boot | Account management and data persistence |
| **Device Microservice** | Spring Boot | Sensor management and user-device mapping |
| **Monitoring Service** | Spring Boot | Hourly consumption calculation and overconsumption alerts |
| **Chat Service** | WebSockets/AI | Hybrid support system (Rule-based + Gemini AI) |
| **Message Broker** | RabbitMQ | Asynchronous communication and data synchronization |

---

## Key Technical Features

### 1. Data Ingestion & Load Balancing
The system processes data from a sensor simulator that generates measurements at 10-minute intervals:
- **Horizontal Scalability**: Utilizes **Docker Swarm** to run multiple replicas of the monitoring service.
- **Load Balancer**: A custom service distributes messages from the RabbitMQ central queue to available replicas using **Round-Robin** strategies.

### 2. Event-Driven Synchronization
- Any changes in the User or Device microservices are propagated asynchronously via RabbitMQ to ensure data consistency across the system without direct coupling.

### 3. Real-Time Interactions
- **WebSockets (STOMP)**: Delivers instant "push" notifications to the client when energy consumption exceeds the configured maximum threshold.
- **AI Support**: Integrated with **Google Gemini 1.5 Flash** to provide automated responses to complex user inquiries when no predefined rules are matched.

---

## Evaluation Scenarios
1. **Consumption Monitoring**: View daily history through line or bar charts (kWh/h).
2. **Synchronization**: Adding a new user/device and verifying automatic propagation to associated service databases.
3. **Intelligent Chat**: Testing the support logic (Keyword matching vs. Generative AI).

---

## Deployment & Requirements
- **Mandatory**: Docker & Docker Compose / Docker Swarm.
- **Environment**: Requires a Google Gemini API Key for the AI support module.

```bash
# Clone the repository
git clone [https://github.com/AlinaaZaharia/energy-management-system.git](https://github.com/AlinaaZaharia/energy-management-system.git)

# Build & Run with Docker
docker-compose up -d --build