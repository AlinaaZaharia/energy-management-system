# Energy Management System (EMS) - Distributed Microservices

## Overview
Acest proiect reprezintă o soluție completă de gestionare a energiei, dezvoltată pe o arhitectură de **microservicii distribuite**. Sistemul permite monitorizarea în timp real a consumului, procesarea asincronă a datelor IoT și suport inteligent pentru clienți prin AI.

---

## System Architecture
Sistemul este construit folosind principii de **loose coupling** și este complet containerizat folosind **Docker**.

| Componentă | Tehnologie | Responsabilitate |
| :--- | :--- | :--- |
| **Frontend** | React.js | Interfață role-based (Admin/Client) și grafice de consum  |
| **API Gateway** | Traefik | Reverse proxy, autentificare JWT și rutare  |
| **User Microservice** | Spring Boot | Managementul conturilor și securitate  |
| **Device Microservice** | Spring Boot | Gestiunea senzorilor și asocierea lor cu utilizatorii  |
| **Monitoring Service** | Spring Boot | Calculul consumului orar și alerte de depășire  |
| **Chat Service** | WebSockets/AI | Suport clienți hibrid (Reguli + Gemini AI)  |
| **Message Broker** | RabbitMQ | Comunicare asincronă și sincronizarea datelor  |

---

## Key Technical Features

### 1. Data Ingestion & Load Balancing
Sistemul procesează date de la un simulator de senzori care generează măsurători la intervale de 10 minute:
- **Scalabilitate Orizontală**: Utilizarea **Docker Swarm** pentru a rula replici multiple ale serviciului de monitorizare.
- **Load Balancer**: Un serviciu personalizat distribuie mesajele din coada RabbitMQ către replicile disponibile folosind strategii de tip Round-Robin.

### 2. Event-Driven Synchronization
- Orice modificare în User sau Device Microservice este propagată asincron prin RabbitMQ către celelalte servicii pentru a asigura consistența datelor fără cuplaj direct.

### 3. Real-Time Interactions
- **WebSockets (STOMP)**: Notificări instantanee de tip "push" către client atunci când consumul depășește pragul maxim setat.
- **AI Support**: Integrare cu **Google Gemini 1.5 Flash** pentru a oferi răspunsuri automate la întrebările complexe ale utilizatorilor.

---

## Evaluation Scenarios
1. **Monitorizare Consum**: Vizualizarea istoricului zilnic sub formă de grafice (kWh/h).
2. **Sincronizare**: Adăugarea unui utilizator nou și verificarea propagării automate în baza de date a dispozitivelor.
3. **Chat Inteligent**: Testarea logicii de chat (Cuvinte cheie vs. AI Generativ).

---

## Deployment & Requirements
- **Mandatory**: Docker & Docker Compose / Docker Swarm.
- **Environment**: Necesită o cheie API Google Gemini pentru modulul de suport AI.

```bash
# Clone the repository
git clone [https://github.com/AlinaaZaharia/energy-management-system.git](https://github.com/AlinaaZaharia/energy-management-system.git)

# Build & Run with Docker
docker-compose up -d --build