# SOC_project
new repo
hellow
## 📦 Student 2 Contribution: Product Catalog Service

### 📌 Overview
The **Product Catalog Service** is an independent microservice responsible for managing product inventory and details. It provides secure REST APIs, uses an in-memory database for fast operations, and is containerized for easy deployment.

---

### 🛠️ Tech Stack
* **Framework:** Spring Boot 4.0.7
* **Language:** Java 17
* **Database:** H2 In-Memory Database
* **ORM:** Spring Data JPA
* **Documentation:** Springdoc OpenAPI (Swagger UI)
* **Containerization:** Docker

---

### 📂 Service Directory Structure
```text
product-service/
├── src/main/java/com/soc/productservice/
│   ├── config/          # ApiKeyFilter for Security
│   ├── controller/      # REST API Endpoints
│   ├── model/           # Product Entity
│   ├── repository/      # JPA Repository Interface
│   └── service/         # Business Logic Layer
├── src/main/resources/  # Application Configuration
└── Dockerfile           # Docker Deployment Configuration
```

### 🔑 Security & Authorization
This service is protected using custom HTTP header authentication:
* **Header Name:** `X-API-KEY`
* **Header Value:** `PRODUCT-SERVICE-SECRET-KEY`
* **Behavior:** Rejects unauthorized requests with 401 Unauthorized.
* **Public Exemption:** Swagger UI (`/swagger-ui.html`) and API Docs (`/api-docs`) remain accessible without API keys.

### 🚀 REST API Endpoints

| Method | Endpoint | Description | Authentication |
| :--- | :--- | :--- | :--- |
| `GET` | `/products` | Fetch all products | `X-API-KEY` Required |
| `GET` | `/products/{id}` | Fetch a single product by ID | `X-API-KEY` Required |
| `POST` | `/products` | Add a new product | `X-API-KEY` Required |
| `DELETE` | `/products/{id}` | Delete a product by ID | `X-API-KEY` Required |
| `GET` | `/swagger-ui.html` | Interactive API Documentation | Public |

### 🐳 Docker Configuration
The service is packaged using Docker on port 8081:

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
# Online E-Commerce & Delivery System (SOC Project)

Microservices-based architecture built with Spring Boot, API Gateway, Auth Service (JWT), Docker, and Swagger.

---

##  Team Member Work Breakdown Matrix

| Student | Microservice / Role | Key Responsibilities | Port | Swagger UI URL |
|---|---|---|:---:|---|
| **Student 1 (Lead)** | API Gateway & Auth Service | Gateway Routing, JWT Auth, API Key Auto-Inject, CORS, Rate Limiting | 8080 / 8084 | `http://localhost:8084/swagger-ui.html` |
| **Student 2** | Product Service | Product & Category CRUD, Inventory | 8081 | `http://localhost:8081/swagger-ui/index.html` |
| **Student 3** | Order Service | Order Management & Cart | 8082 | `http://localhost:8082/swagger-ui/index.html` |
| **Student 4** | Payment Service | Transaction Processing & Payment History | 8083 | `http://localhost:8083/swagger-ui/index.html` |
| **Student 5** | Notification Service | Email & SMS Order Notifications | 8085 | `http://localhost:8085/swagger-ui/index.html` |

---


##  API Gateway & Auth Service (Student 1 Details)

Student 1 is responsible for the single entry point (**API Gateway**) and centralized authentication (**Auth Service**).

###  1. Auth Service Details
- **Port:** `8084`
- **Interactive Swagger UI:** [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)
- **Database:** H2 In-Memory Database (`jdbc:h2:mem:authdb`)
- **Security:** Spring Security with BCrypt Password Hashing and JJWT (JSON Web Token) generation & validation.

####  Auth Service REST API Endpoints

| HTTP Method | Endpoint | Description | Authorization Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user account | None (Public) |
| `POST` | `/api/auth/login` | Authenticate user and receive JWT token | None (Public) |
| `GET` | `/api/auth/validate` | Validate a JWT token (`?token=...`) | None (Public) |

---

###  2. API Gateway Details
- **Port:** `8080` (Main System Entry Point)
- **Framework:** Spring Cloud Gateway (Reactive WebFlux)
- **Key Features:**
  1. **Dynamic Routing:**
     - `/api/auth/**` ➔ Auth Service (`http://localhost:8084`)
     - `/api/products/**` ➔ Product Service (`http://localhost:8081`)
     - `/api/orders/**` ➔ Order Service (`http://localhost:8082`)
     - `/api/payments/**` ➔ Payment Service (`http://localhost:8083`)
     - `/api/notifications/**` ➔ Notification Service (`http://localhost:8085`)
  2. **JWT Authentication Gateway Filter:** Validates the `Authorization: Bearer <token>` header for protected microservice endpoints. Extracts user info and injects `X-User-Name` & `X-User-Role` downstream.
  3. **Auto-Inject X-API-KEY Filter:** Automatically injects each downstream service's specific `X-API-KEY` header (`PRODUCT-SERVICE-SECRET-KEY`, `payment-secret-key-123`, `notification-secret-key-123`, etc.) so clients do not need internal keys.
  4. **CORS & Rate Limiting:** Global CORS configuration for frontend clients + in-memory rate limiter (60 req/min per IP) to protect against DDoS/abuse.

---

###  Example API Gateway Testing Flow (cURL)

**Step A: Register a New User**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
        "username": "john_doe",
        "password": "Password123!",
        "email": "john@example.com",
        "role": "ROLE_USER"
      }'
```

**Step B: Login & Get JWT Token**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
        "username": "john_doe",
        "password": "Password123!"
      }'
```
*Response returns a JSON containing `"token": "eyJhbGciOiJIUzI1NiJ9..."`.*

**Step C: Call Protected Microservice Endpoint Through Gateway**
```bash
curl -X GET http://localhost:8080/api/payments/history/5 \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```
*Gateway validates JWT and automatically injects `X-API-KEY: payment-secret-key-123` to Payment Service.*

---


##  Payment Service (Student 4 Details)

The **Payment Service** processes customer payment transactions, stores transaction history, manages payment statuses (`COMPLETED`, `FAILED`, `REFUNDED`), and handles refund processing.

###  Microservice Details
- **Port:** `8083`
- **Interactive Swagger UI:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- **Database:** H2 In-Memory Database (`jdbc:h2:mem:paymentdb`)

###  API Key Security Verification
Every direct request to the Payment Service endpoints must include the `X-API-KEY` header:
- **Header Key:** `X-API-KEY`
- **Header Value:** `payment-secret-key-123`

*Requests missing or providing an invalid API Key will receive a `401 Unauthorized` HTTP response.*

###  REST API Endpoints

| HTTP Method | Endpoint | Description | Header Required |
|---|---|---|---|
| `POST` | `/api/payments/process` | Process a new payment transaction | `X-API-KEY: payment-secret-key-123` |
| `GET` | `/api/payments/history/{userId}` | Retrieve payment history for a user | `X-API-KEY: payment-secret-key-123` |
| `GET` | `/api/payments/{id}` | Get payment details by payment ID | `X-API-KEY: payment-secret-key-123` |
| `GET` | `/api/payments/transaction/{transactionId}` | Get payment details by transaction ID | `X-API-KEY: payment-secret-key-123` |
| `POST` | `/api/payments/refund/{id}` | Process refund for an existing payment | `X-API-KEY: payment-secret-key-123` |

###  Example API Requests (cURL)

**1. Process Payment:**
```bash
curl -X POST http://localhost:8083/api/payments/process \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: payment-secret-key-123" \
  -d '{
        "orderId": 101,
        "userId": 5,
        "amount": 2500.00,
        "paymentMethod": "CREDIT_CARD",
        "currency": "LKR"
      }'
```

**2. Get Payment History for User:**
```bash
curl -X GET http://localhost:8083/api/payments/history/5 \
  -H "X-API-KEY: payment-secret-key-123"
```

**3. Refund Payment:**
```bash
curl -X POST http://localhost:8083/api/payments/refund/1 \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: payment-secret-key-123" \
  -d '{ "reason": "Customer cancelled order" }'
```

###  How to Run Payment Service Locally

```bash
cd payment-service
.\mvnw spring-boot:run
```

---


##  Notification Service (Student 5 Details)

The **Notification Service** handles sending order status updates and receipts via Email and SMS.

###  Microservice Details
- **Port:** `8085`
- **Interactive Swagger UI:** [http://localhost:8085/swagger-ui/index.html](http://localhost:8085/swagger-ui/index.html)
- **Database:** H2 In-Memory Database (`jdbc:h2:mem:notificationdb`)

###  API Key Security Verification
Every direct request to the Notification Service endpoints must include the `X-API-KEY` header:
- **Header Key:** `X-API-KEY`
- **Header Value:** `notification-secret-key-123`

*Requests missing or providing an invalid API Key will receive a `401 Unauthorized` HTTP response.*

###  REST API Endpoints
| HTTP Method | Endpoint | Description | Header Required |
|---|---|---|---|
| `POST` | `/api/notifications/email` | Send email notification | `X-API-KEY: notification-secret-key-123` |
| `POST` | `/api/notifications/sms` | Send SMS notification | `X-API-KEY: notification-secret-key-123` |
| `GET` | `/api/notifications/user/{userId}` | Get notification history for user | `X-API-KEY: notification-secret-key-123` |
| `GET` | `/api/notifications/{id}` | Get notification details by ID | `X-API-KEY: notification-secret-key-123` |

###  How to Run Notification Service Locally
```bash
cd notification-service
.\mvnw spring-boot:run
```

---

##  Docker Deployment (Entire Ecosystem)

To build and run all microservices with Docker Compose:

```bash
docker compose up --build
```
