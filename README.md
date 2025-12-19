# Energy Management System (EMS) - Distributed Microservices

## Overview
[cite_start]Acest proiect reprezintă o soluție completă de gestionare a energiei, dezvoltată pe o arhitectură de **microservicii distribuite**[cite: 20]. [cite_start]Sistemul permite monitorizarea în timp real a consumului, procesarea asincronă a datelor IoT și suport inteligent pentru clienți prin AI[cite: 137, 244, 264].

---

## System Architecture
[cite_start]Sistemul este construit folosind principii de **loose coupling** și este complet containerizat folosind **Docker**[cite: 20, 28].

| Componentă | Tehnologie | Responsabilitate |
| :--- | :--- | :--- |
| **Frontend** | React.js | [cite_start]Interfață role-based (Admin/Client) și grafice de consum [cite: 22, 63, 193] |
| **API Gateway** | Traefik | [cite_start]Reverse proxy, autentificare JWT și rutare [cite: 51, 63, 291] |
| **User Microservice** | Spring Boot | [cite_start]Managementul conturilor și securitate [cite: 59, 63] |
| **Device Microservice** | Spring Boot | [cite_start]Gestiunea senzorilor și asocierea lor cu utilizatorii [cite: 61, 63] |
| **Monitoring Service** | Spring Boot | [cite_start]Calculul consumului orar și alerte de depășire [cite: 143, 168] |
| **Chat Service** | WebSockets/AI | [cite_start]Suport clienți hibrid (Reguli + Gemini AI) [cite: 247, 264] |
| **Message Broker** | RabbitMQ | [cite_start]Comunicare asincronă și sincronizarea datelor [cite: 142, 165] |

---

## Key Technical Features

### 1. Data Ingestion & Load Balancing
[cite_start]Sistemul procesează date de la un simulator de senzori care generează măsurători la intervale de 10 minute[cite: 140, 155]:
- [cite_start]**Scalabilitate Orizontală**: Utilizarea **Docker Swarm** pentru a rula replici multiple ale serviciului de monitorizare[cite: 292].
- [cite_start]**Load Balancer**: Un serviciu personalizat distribuie mesajele din coada RabbitMQ către replicile disponibile folosind strategii de tip Round-Robin[cite: 287].

### 2. Event-Driven Synchronization
- [cite_start]Orice modificare în User sau Device Microservice este propagată asincron prin RabbitMQ către celelalte servicii pentru a asigura consistența datelor fără cuplaj direct[cite: 144, 145].

### 3. Real-Time Interactions
- [cite_start]**WebSockets (STOMP)**: Notificări instantanee de tip "push" către client atunci când consumul depășește pragul maxim setat[cite: 267, 314].
- [cite_start]**AI Support**: Integrare cu **Google Gemini 1.5 Flash** pentru a oferi răspunsuri automate la întrebările complexe ale utilizatorilor[cite: 264, 291].

---

## Evaluation Scenarios
1. [cite_start]**Monitorizare Consum**: Vizualizarea istoricului zilnic sub formă de grafice (kWh/h)[cite: 193].
2. [cite_start]**Sincronizare**: Adăugarea unui utilizator nou și verificarea propagării automate în baza de date a dispozitivelor[cite: 146, 147].
3. [cite_start]**Chat Inteligent**: Testarea logicii de chat (Cuvinte cheie vs. AI Generativ)[cite: 259, 261, 264].

---

## Deployment & Requirements
- [cite_start]**Mandatory**: Docker & Docker Compose / Docker Swarm[cite: 64, 292].
- [cite_start]**Environment**: Necesită o cheie API Google Gemini pentru modulul de suport AI[cite: 264, 291].

```bash
# Clone the repository
git clone [https://github.com/AlinaaZaharia/energy-management-system.git](https://github.com/AlinaaZaharia/energy-management-system.git)

# Build & Run with Docker
docker-compose up -d --build