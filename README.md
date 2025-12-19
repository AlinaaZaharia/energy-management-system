Energy Management System (EMS) - Distributed Microservices Architecture
Overview
Acest proiect reprezintă un sistem complex de gestionare și monitorizare a consumului de energie, construit pe o arhitectură de microservicii scalabilă. Sistemul permite monitorizarea în timp real a dispozitivelor IoT, procesarea asincronă a datelor masive prin mecanisme de Load Balancing și oferă suport interactiv utilizatorilor prin tehnologii WebSockets și AI Generativ.
+2

Core Features
1. Distributed Data Ingestion & Load Balancing
Sistemul este proiectat pentru a gestiona volume mari de date provenite de la senzori printr-un pipeline de ingestie scalabil:


Custom Load Balancer: Un serviciu dedicat care consumă datele din coada centrală RabbitMQ (device.measurements) și distribuie sarcina folosind un algoritm Round-Robin către multiple replici de monitorizare.
+1


Horizontal Scaling: Suportă rularea simultană a multiple instanțe de Monitoring & Communication Microservice, fiecare având propria coadă de ingestie (monitoring_q_n).
+1

2. Real-Time Communication & Notifications

WebSockets (STOMP): Asigură un canal de comunicare persistent pentru livrarea instantanee a alertelor de supraconsum către platforma de vizualizare.
+1


Overconsumption Alerts: Monitorizarea în timp real detectează depășirea pragurilor de consum și notifică imediat utilizatorul prin mesaje de tip "Toast" în frontend.
+1

3. AI-Driven Customer Support
Sistemul integrează un centru de suport inteligent pentru clienți:


Hybrid Chat System: Combină un motor bazat pe reguli (pentru întrebări frecvente) cu un model de limbaj avansat (Google Gemini 1.5 Flash) pentru răspunsuri contextuale complexe.
+1


Admin-Client Interaction: Permite intervenția manuală a administratorilor în sesiunile de chat active.
+1


Typing Indicator: Feedback vizual în timp real pentru îmbunătățirea experienței utilizatorului.

System Architecture
Microservices

User Management: Gestionează identitățile și rolurile utilizatorilor (Admin/Client).


Device Management: Administrează inventarul dispozitivelor smart și asocierea acestora cu utilizatorii.


Monitoring & Communication: Procesează fluxurile de date, calculează totalurile orare și emite notificări.
+1


Communication Service: Gestionează transportul mesajelor chat și al notificărilor WebSockets.
+1


API Gateway (Traefik): Punct unic de intrare care asigură rutarea, autentificarea (JWT) și securizarea cererilor.
+2

Data Pipeline & Synchronization

Message Broker (RabbitMQ): Decuplează microserviciile și asigură sincronizarea datelor (Users/Devices) între baze de date diferite pentru a menține consistența.
+2

Tech Stack
Backend: Java Spring Boot (Microservices)

Frontend: React.js, SockJS, STOMP

Messaging: RabbitMQ

AI: Google Gemini API


Infrastructure: Docker, Docker Swarm (pentru replicare și load balancing) 
+1

Reverse Proxy: Traefik


Databases: PostgreSQL / MySQL (per-service database pattern) 

Getting Started
Prerequisites
Docker & Docker Compose

Google Gemini API Key

Installation & Deployment
Clone the repository:

Bash

git clone https://github.com/AlinaaZaharia/energy-management-system.git
Build the Frontend:

Bash

cd frontend-app && npm install && npm run build
Deploy with Docker:

Bash

docker-compose up -d --build
Evaluation Scenarios

Load Balancing: Monitorizarea log-urilor serviciilor de monitorizare (monitoring-service-1, monitoring-service-2) pentru a observa distribuția alternativă a mesajelor primite de la simulator.

Chat Support: Testarea fluxului de la Rule-based la AI prin trimiterea de mesaje generale, respectiv tehnice.


Real-Time Alerts: Simularea unui consum ridicat (peste limita dispozitivului) pentru a declanșa notificarea WebSocket în interfața clientului.