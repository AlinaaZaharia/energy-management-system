# Sistem de Management Energetic – Sisteme Distribuite Tema 3

## Comunicare Real-Time si Scalabilitate

### Prezentare Generala

Aceasta tema extinde sistemul realizat in etapele anterioare prin adaugarea de capabilitati de comunicare in timp real si scalabilitate orizontala. Sistemul integreaza acum un modul de Chat inteligent (bazat pe reguli si AI), notificari push prin WebSockets si un mecanism de Load Balancing pentru procesarea datelor provenite de la senzorii IoT.

Arhitectura a fost imbunatatita pentru a suporta un flux mare de date prin distribuirea incarcarii catre multiple instante ale serviciului de monitorizare.

---

## Arhitectura

### Componente Noi si Actualizate

- **Communication Service**: Microserviciu nou dedicat chat-ului si notificarilor (WebSocket).
- **Load Balancer Service**: Microserviciu nou care distribuie traficul de la simulator catre replicile de monitorizare.
- **Monitoring Service (Replica 1 & 2)**: Serviciul de monitorizare a fost duplicat pentru a demonstra scalabilitatea.
- **AI Integration**: Integrare cu Google Gemini 1.5 Flash pentru suport automatizat in chat.
- **Frontend**: Extins cu componente de Chat (SockJS/STOMP) si notificari "toast".

### Fluxuri de Date

1. **IoT Data Pipeline**: Simulator -> Load Balancer -> Cozi Dedicate (Round Robin) -> Monitoring Replicas -> DB.
2. **Real-Time Notification**: Monitoring Service -> RabbitMQ -> Communication Service -> WebSocket -> Client Frontend.
3. **Chat System**: Client -> WebSocket -> Communication Service -> (Reguli / AI / Admin) -> Client.

### Tehnologii Adaugate

- **Real-time**: WebSockets, SockJS, STOMP
- **AI**: Google Gemini API (model `gemini-1.5-flash`)
- **Load Balancing**: Custom Application-Level Balancer (Spring Boot)
- **Scalare**: Docker Compose (servicii replicate)

---

## Cerinte

- Docker 24.x
- Docker Compose 2.x
- Git
- Cheie API Google Gemini (configurabila in env)

---

## Instructiuni Build si Rulare

### 1. Clone repository

git clone repository-url
cd DS2025_Grupa_Nume_Prenume_Assignment_3

### 2. Build frontend

cd sd-frontend
npm install
npm run build

### 3. Copiere build in nginx

Windows
Copy-Item -Recurse -Force dist\* ..\deployment-sd\nginx\html\

Linux / Mac
cp -r dist/* ../deployment-sd/nginx/html/

### 4. Pornire servicii

cd ../deployment-sd
docker-compose down
docker-compose up -d --build

### 5. Verificare rulare

docker ps
# Ar trebui sa vezi load-balancer, monitoring-service-1, monitoring-service-2, communication-service etc.

---

## Functionalitati Noi

### 1. Load Balancing si Scalabilitate
Sistemul asigura procesarea paralela a masuratorilor:
- **Load Balancer**: Primeste toate mesajele din coada `device.measurements`.
- **Distributie**: Aplica un algoritm Round-Robin pentru a trimite mesajele alternativ catre `monitoring_q_1` si `monitoring_q_2`.
- **Replicare**: Doua instante de Monitoring Service ruleaza simultan, fiecare ascultand pe coada sa dedicata, dar scriind in aceeasi baza de date.

### 2. Chat Suport Client
Interfata de chat disponibila pentru utilizatorii logati, cu 3 niveluri de raspuns:
- **Nivel 1 (Reguli)**: Raspunsuri instantanee pentru cuvinte cheie (ex: "bill", "consumption", "hello").
- **Nivel 2 (AI)**: Daca nu exista o regula, mesajul este procesat de Google Gemini API pentru a oferi un raspuns contextual.
- **Nivel 3 (Admin)**: Administratorul poate vedea conversatiile active si poate interveni manual. Mesajele adminului au prioritate.

### 3. Notificari in Timp Real
- Daca un dispozitiv depaseste limita de consum orar configurata, serviciul de monitorizare emite un eveniment.
- Utilizatorul primeste instant o notificare "Toast" (fereastra rosie) in interfata web, fara a fi nevoie de refresh (via WebSocket).

### 4. Chat Typing Indicator
- Administratorul poate vedea in timp real cand un utilizator scrie un mesaj ("user is typing...").

---

## Configurare Variabile Mediu (Docker Compose)

### Load Balancer
SPRING_RABBITMQ_HOST=rabbitmq
SERVER_PORT=8080

### Monitoring Replicas
Monitoring Service 1:
- MONITORING_QUEUE_NAME=monitoring_q_1

Monitoring Service 2:
- MONITORING_QUEUE_NAME=monitoring_q_2

### Communication Service (AI)
In clasa `AIService.java` sau variabile de mediu:
- API_KEY=YOUR_GOOGLE_GEMINI_KEY

---

## Testare Scenarii

### Testare Load Balancing
Se pot urmari log-urile pentru a vedea distributia:
docker logs -f load-balancer
docker logs -f monitoring-service-1
docker logs -f monitoring-service-2

Se va observa ca Load Balancer-ul alterneaza trimiterea mesajelor, iar replicile prelucreaza doar mesajele din cozile proprii.

### Testare Chat
1. Login ca User -> Scrie "Hello" (Raspuns regula).
2. Scrie "What is energy?" (Raspuns AI Gemini).
3. Login ca Admin (in Incognito) -> Selecteaza userul -> Scrie un raspuns manual.

---

## Structura Cozi RabbitMQ

- `device.measurements`: Coada de intrare de la simulator (consumata de LB).
- `monitoring_q_1`: Coada dedicata Replicii 1.
- `monitoring_q_2`: Coada dedicata Replicii 2.
- `notification.queue`: Coada pentru alerte de supraconsum.
- `sync.events`: Sincronizare date intre microservicii.

---

## Concepte Implementate Tema 3

- **Load Balancing Application-Level**
- **WebSockets & STOMP Protocol**
- **LLM Integration (Generative AI)**
- **Distributed Queues Architecture**
- **Scalabilitate Orizontala**
- **Secure Token Propagation in Headers**

---

## Oprire Sistem

docker-compose down