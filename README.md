# Online E-Commerce & Delivery System (SOC Project)

Microservices-based architecture built with Spring Boot, API Gateway, Docker, and Swagger.

---

## 📌 Team Member Work Breakdown Matrix

| Student | Microservice / Role | Key Responsibilities | Port | Swagger UI URL |
|---|---|---|:---:|---|
| **Student 1 (Lead)** | API Gateway & Auth Service | Gateway Routing, OAuth 2.0, Rate Limiting | 8080 | `http://localhost:8080/swagger-ui.html` |
| **Student 2** | Product Service | Product & Category CRUD, Inventory | 8081 | `http://localhost:8081/swagger-ui/index.html` |
| **Student 3** | Order Service | Order Management & Cart | 8082 | `http://localhost:8082/swagger-ui/index.html` |
| **Student 4** | Payment Service | Transaction Processing & Payment History | 8083 | `http://localhost:8083/swagger-ui/index.html` |
| **Student 5** | Notification Service | Email & SMS Order Notifications | 8085 | `http://localhost:8085/swagger-ui/index.html` |

---

## 🔔 Notification Service (Student 5 Details)

The **Notification Service** handles sending order status updates and receipts via Email and SMS.

### 🌐 Microservice Details
- **Port:** `8085`
- **Interactive Swagger UI:** [http://localhost:8085/swagger-ui/index.html](http://localhost:8085/swagger-ui/index.html)
- **Database:** H2 In-Memory Database (`jdbc:h2:mem:notificationdb`)

### 🔐 API Key Security Verification
Every direct request to the Notification Service endpoints must include the `X-API-KEY` header:
- **Header Key:** `X-API-KEY`
- **Header Value:** `notification-secret-key-123`

*Requests missing or providing an invalid API Key will receive a `401 Unauthorized` HTTP response.*

### 🛠️ REST API Endpoints
| HTTP Method | Endpoint | Description | Header Required |
|---|---|---|---|
| `POST` | `/api/notifications/email` | Send email notification | `X-API-KEY: notification-secret-key-123` |
| `POST` | `/api/notifications/sms` | Send SMS notification | `X-API-KEY: notification-secret-key-123` |
| `GET` | `/api/notifications/user/{userId}` | Get notification history for user | `X-API-KEY: notification-secret-key-123` |
| `GET` | `/api/notifications/{id}` | Get notification details by ID | `X-API-KEY: notification-secret-key-123` |

### 🚀 How to Run Notification Service Locally
```bash
cd notification-service
.\mvnw spring-boot:run
```
