<div align="center">

# ðŸ›’ SOC â€” Online E-Commerce & Delivery System

### Enterprise-grade, security-hardened microservices platform

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.3-brightgreen?style=for-the-badge&logo=springboot)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud-Gateway-blue?style=for-the-badge&logo=spring)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Security](https://img.shields.io/badge/Security-Zero%20Trust%20Hardened-brightgreen?style=for-the-badge&logo=shield)
![OWASP](https://img.shields.io/badge/OWASP-Dependency--Check-E74C3C?style=for-the-badge)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-success?style=for-the-badge&logo=githubactions)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger_UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

</div>

---

## ðŸ“‹ Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Technology Stack](#-technology-stack)
- [Microservices & Database Architecture](#-microservices--database-architecture)
- [Security Hardening](#-security-hardening)
- [Quick Start](#-quick-start)
- [Service URLs & Swagger UI](#-service-urls--swagger-ui)
- [REST API Reference](#-rest-api-reference)
- [API Testing Guide](#-api-testing-guide)
- [Project Structure](#-project-structure)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [License](#-license)

---

## ðŸŒ Overview

**SOC** is a fully secured, enterprise-grade **Online E-Commerce & Delivery Platform** built as a distributed microservices system using **Java 17** and **Spring Boot 3.2.3**. All external traffic is funneled through a hardened **Spring Cloud Gateway** that enforces rate-limiting, path-traversal rejection, JWT signature validation, and role header injection â€” before requests ever reach an internal service.

The platform was developed as part of the **SOC (Service-Oriented Computing)** course and demonstrates production-grade patterns including Zero-Trust networking, OWASP-aligned security hardening, CI/CD pipelines, and database-per-service isolation.

---

## âœ¨ Key Features

- **5 Independent Microservices** â€” each with its own isolated MongoDB instance, no shared state
- **Zero-Trust Security Model** â€” JWT Bearer tokens, constant-time API key validation, RBAC, and BOLA/IDOR ownership checks at every layer
- **HMAC-SHA256 Webhook Signatures** â€” cryptographically verified payment callbacks
- **14 Security Vulnerabilities Resolved** â€” comprehensive OWASP audit with phased remediation
- **Sliding-Window Rate Limiting** â€” 60 req/min per IP with anti-X-Forwarded-For spoofing
- **Docker Compose One-Command Deployment** â€” 11 containers (6 services + 5 MongoDB instances) on an isolated private bridge network
- **Full CI/CD Pipeline** â€” GitHub Actions with automated tests and OWASP Dependency-Check on every push
- **Swagger UI** on every service for interactive API exploration
- **DTO Anti-Mass-Assignment Layer** â€” decoupled request/response contracts with Jakarta Bean Validation
- **Profile-Gated Data Seeders** â€” `@Profile("!prod")` ensures dev fixtures never run in production

---

## ðŸ“ System Architecture

All client requests enter through the centralized **Spring Cloud Gateway** (`Port 8080`). The Gateway enforces rate limiting with trusted proxy validation, rejects path traversal sequences, validates JWT signatures, and enriches requests with user identity headers before forwarding them over the private bridge network (`soc-internal-net`).

```mermaid
flowchart TD
    Client[ðŸ“± Client App / Postman / Frontend]

    subgraph GatewayLayer ["ðŸšª Perimeter Gateway Layer"]
        Gateway["âš™ï¸ API Gateway (Port 8080)<br/>â€¢ AntPathMatcher Whitelist<br/>â€¢ Anti-Traversal Protection (.., ;, %2f)<br/>â€¢ Sliding-Window Rate Limiter (60 req/min)<br/>â€¢ JWT Validation & Downstream Header Injection<br/>â€¢ Restricted CORS (localhost:3000 only)"]
    end

    subgraph InternalNetwork ["ðŸ”’ Isolated Docker Network (soc-internal-net)"]
        AuthService["ðŸ” Auth Service (Port 8084)<br/>â€¢ JWT Generation (HMAC-SHA256)<br/>â€¢ BCrypt Password Hashing<br/>â€¢ Non-Prod Seeders (@Profile('!prod'))"]
        ProductService["ðŸ“¦ Product Service (Port 8081)<br/>â€¢ Catalog CRUD & Admin RBAC<br/>â€¢ Constant-Time API Key Validation"]
        OrderService["ðŸ›’ Order Service (Port 8082)<br/>â€¢ Order Placement & Lifecycle<br/>â€¢ Ownership Verification (BOLA/IDOR)<br/>â€¢ HMAC-SHA256 Webhook Signatures"]
        PaymentService["ðŸ’³ Payment Service (Port 8083)<br/>â€¢ Payment DTO Layer & Validation<br/>â€¢ Admin-Only Refunds<br/>â€¢ User History Ownership Verification"]
        NotificationService["ðŸ”” Notification Service (Port 8085)<br/>â€¢ Decoupled DTO Dispatches<br/>â€¢ Email & SMS Alert Logs"]

        DB1[(auth_db)]
        DB2[(product_db)]
        DB3[(order_db)]
        DB4[(payment_db)]
        DB5[(notification_db)]

        AuthService --- DB1
        ProductService --- DB2
        OrderService --- DB3
        PaymentService --- DB4
        NotificationService --- DB5
    end

    Client -->|HTTP / REST| Gateway
    Gateway -->|/api/auth/**| AuthService
    Gateway -->|/api/products/**| ProductService
    Gateway -->|/api/v1/orders/**| OrderService
    Gateway -->|/api/payments/**| PaymentService
    Gateway -->|/api/notifications/**| NotificationService
```

### Request Lifecycle

Every request passes through a hardened filter chain before reaching business logic:

```mermaid
sequenceDiagram
    participant C as Client
    participant RL as RateLimitingFilter
    participant JWT as JwtAuthenticationFilter
    participant SVC as Downstream Service
    participant CTRL as Controller + RBAC

    C->>RL: HTTP Request + Bearer Token
    RL->>RL: Resolve real IP (trusted proxy subnet check)
    RL->>RL: Check sliding-window count

    alt Rate limit exceeded (>60/min)
        RL-->>C: 429 Too Many Requests (Retry-After: 60)
    else Within limit
        RL->>JWT: Forward request
        JWT->>JWT: Check path traversal (.., ;, %2f)
        alt Traversal detected
            JWT-->>C: 401 Forbidden path
        else Whitelisted endpoint
            JWT->>SVC: Pass-through (no JWT required)
        else Protected endpoint
            JWT->>JWT: Validate Bearer JWT (HS256)
            JWT->>SVC: Enriched request (X-User-Name, X-User-Role, X-API-KEY)
            SVC->>SVC: Constant-time MessageDigest.isEqual(X-API-KEY)
            CTRL->>CTRL: RBAC role check + IDOR ownership check
            CTRL-->>C: 200 OK + Response Body
        end
    end
```

---

## ðŸ—ï¸ Technology Stack

| Component | Technology | Version |
|---|---|:---:|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.3 |
| API Gateway | Spring Cloud Gateway | 2023.0.0 |
| JWT Library | JJWT | 0.11.5 |
| Database | MongoDB | 7.0 |
| Containerization | Docker + Docker Compose | 3.8 |
| API Documentation | SpringDoc OpenAPI / Swagger UI | â€” |
| Code Generation | Lombok | â€” |
| Bean Validation | Jakarta Validation | â€” |
| Testing | JUnit 5 + Mockito + Reactor Test | â€” |
| Vulnerability Scan | OWASP Dependency-Check Maven Plugin | 9.0.9 |
| CI/CD | GitHub Actions | â€” |
| Build Tool | Maven Wrapper (`mvnw`) | â€” |

---

## ðŸ‘¥ Microservices & Database Architecture

The platform follows the **Database-per-Service** pattern. Each microservice owns a dedicated MongoDB container that does **not** expose a public host port â€” all database communication happens exclusively within the `soc-internal-net` private bridge network with root authentication enabled.

| Role | Microservice | Service Port | Database | Internal Mongo Host | Security Layer |
|---|---|:---:|:---:|---|---|
| **Lead â€” Gateway & Auth** | API Gateway + Auth Service | `8080` / `8084` | `auth_db` | `mongo-auth:27017` | JWT, AntPathMatcher Whitelist, Rate Limiter |
| **Product Catalog** | Product Service | `8081` | `product_db` | `mongo-product:27017` | `X-API-KEY` Constant-Time, `ROLE_ADMIN` RBAC |
| **Order Management** | Order Service | `8082` | `order_db` | `mongo-order:27017` | `X-API-KEY`, HMAC-SHA256 Webhooks, BOLA/IDOR |
| **Payment Processing** | Payment Service | `8083` | `payment_db` | `mongo-payment:27017` | `X-API-KEY`, DTO Layer, Admin-Only Refunds |
| **Notifications** | Notification Service | `8085` | `notification_db` | `mongo-notification:27017` | `X-API-KEY`, DTO Encapsulation, Bean Validation |

---

## ðŸ›¡ï¸ Security Hardening

All **14 security vulnerabilities** identified in the [Security & QA Audit Report](docs/security-audit-report.md) have been resolved across 4 phased patches:

| Phase | Security Patch | Status |
|---|---|:---:|
| **1** | Database network isolation â€” no public host ports on MongoDB containers | âœ… |
| **1** | `AntPathMatcher` whitelist + path traversal hardening in Gateway JWT filter | âœ… |
| **1** | All secrets externalized to `.env` with `@Value` injection | âœ… |
| **1** | Constant-time `MessageDigest.isEqual` API key validation (prevents timing attacks) | âœ… |
| **2** | `ROLE_ADMIN` RBAC on product write/delete and payment refund endpoints | âœ… |
| **2** | BOLA/IDOR ownership checks on orders and payment history | âœ… |
| **2** | HMAC-SHA256 webhook signature verification for payment callbacks | âœ… |
| **2** | CORS restricted to explicit trusted origins (`localhost:3000`) | âœ… |
| **2** | Rate limiting with trusted proxy subnet validation (anti-`X-Forwarded-For` spoofing) | âœ… |
| **3** | `@Profile("!prod")` guard on all `DataInitializer` seeders | âœ… |
| **3** | Decoupled DTO layer for Payment & Notification (anti-mass assignment) | âœ… |
| **3** | Centralized `GlobalExceptionHandler` across all services | âœ… |
| **4** | Unit & Mockito regression test suites for all microservices | âœ… |
| **4** | GitHub Actions CI/CD + OWASP Dependency-Check integration | âœ… |

> See [Security Architecture](docs/architecture/security.md) and [Remediation Plan](docs/remediation-plan.md) for full details.

---

## ðŸš€ Quick Start

### Prerequisites

| Tool | Version | Purpose |
|---|---|---|
| Docker Desktop | 24+ | Container runtime and orchestration |
| Java JDK | 17+ | Local development |
| Maven | 3.8+ (or use included `mvnw`) | Build tool |
| Git | any | Source code management |
| MongoDB Compass | any | Optional â€” GUI for database inspection |

### 1. Clone the Repository

```bash
git clone https://github.com/mrawfulshadow/SOC_project.git
cd SOC_project
```

### 2. Configure Environment Secrets

```bash
cp .env.example .env
```

Edit `.env` and fill in your secrets:

```env
JWT_SECRET=<base64-encoded-hmac-key>
PRODUCT_SERVICE_API_KEY=<your-product-key>
ORDER_SERVICE_API_KEY=<your-order-key>
PAYMENT_SERVICE_API_KEY=<your-payment-key>
NOTIFICATION_SERVICE_API_KEY=<your-notification-key>
WEBHOOK_SECRET=<your-hmac-webhook-secret>
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=<strong-password>
```

> **Production:** Set `SPRING_PROFILES_ACTIVE=prod` to disable `DataInitializer` seeders and prevent default accounts from being created.

### 3. Launch All Services

```bash
# Build and start all 11 containers (6 services + 5 MongoDB instances)
docker compose up --build -d

# Verify all containers are healthy
docker compose ps

# Stream logs
docker compose logs -f
```

### 4. Run Tests & Security Scans

```bash
# Unit and integration tests
mvn clean test

# OWASP Dependency Vulnerability Scan
mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=8
```

### 5. Stop Services

```bash
docker compose down        # Stop (preserves volumes)
docker compose down -v     # Stop and remove all data
```

> For local Maven development, per-service logs, and troubleshooting, see the [Deployment Guide](docs/guides/deployment.md).

---

## ðŸ”Œ Service URLs & Swagger UI

| Service | Local URL | Swagger UI |
|---|---|---|
| **API Gateway** | http://localhost:8080 | â€” |
| **Auth Service** | http://localhost:8084 | â€” |
| **Product Service** | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| **Order Service** | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| **Payment Service** | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| **Notification Service** | http://localhost:8085 | http://localhost:8085/swagger-ui.html |
| **Client App** | http://localhost:3000 | â€” |

### MongoDB Compass (Direct Database Inspection)

| Service | Database | Compass URI |
|---|---|---|
| Auth Service | `auth_db` | `mongodb://localhost:27022` |
| Product Service | `product_db` | `mongodb://localhost:27018` |
| Order Service | `order_db` | `mongodb://localhost:27019` |
| Payment Service | `payment_db` | `mongodb://localhost:27020` |
| Notification Service | `notification_db` | `mongodb://localhost:27021` |

---

## ðŸ“¡ REST API Reference

### ðŸšª API Gateway (`Port 8080`)

| Route Prefix | Forwarded To | Security Applied |
|---|---|---|
| `/api/auth/**` | Auth Service `:8084` | Whitelist (public) |
| `/api/products/**`, `/products/**` | Product Service `:8081` | JWT + X-API-KEY injection |
| `/api/v1/orders/**` | Order Service `:8082` | JWT + X-API-KEY injection |
| `/api/payments/**` | Payment Service `:8083` | JWT + X-API-KEY injection |
| `/api/notifications/**` | Notification Service `:8085` | JWT + X-API-KEY injection |

---

### ðŸ” Auth Service (`Port 8084`)

**Database:** `auth_db` Â· **Security:** Spring Security + BCrypt + JJWT HMAC-SHA256

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user account | Public |
| `POST` | `/api/auth/login` | Authenticate & receive JWT | Public |
| `GET` | `/api/auth/validate` | Verify JWT token validity | Public |

---

### ðŸ“¦ Product Service (`Port 8081`)

**Database:** `product_db` Â· **Security:** Constant-time `X-API-KEY` + `ROLE_ADMIN` RBAC

| Method | Endpoint | Description | Auth | Role |
|---|---|---|---|:---:|
| `GET` | `/api/products` | List all products | JWT Bearer | Any |
| `GET` | `/api/products/{id}` | Get product by ID | JWT Bearer | Any |
| `POST` | `/api/products` | Create new product | JWT Bearer | `ROLE_ADMIN` |
| `DELETE` | `/api/products/{id}` | Delete product | JWT Bearer | `ROLE_ADMIN` |

---

### ðŸ›’ Order Service (`Port 8082`)

**Database:** `order_db` Â· **Security:** BOLA/IDOR ownership checks + HMAC-SHA256 webhook verification

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/orders` | Place a new order | JWT Bearer |
| `GET` | `/api/v1/orders` | List orders (own, or all if Admin) | JWT Bearer |
| `GET` | `/api/v1/orders/{id}` | Fetch order (owner or Admin only) | JWT Bearer |
| `PATCH` | `/api/v1/orders/{id}/status` | Update order status | JWT Bearer |
| `PATCH` | `/api/v1/orders/{id}/delivery` | Update delivery tracking info | JWT Bearer |
| `DELETE` | `/api/v1/orders/{id}` | Cancel order (owner or Admin) | JWT Bearer |
| `POST` | `/api/v1/orders/{id}/payment-webhook` | Payment callback (HMAC-SHA256 required) | `X-Signature-SHA256` |

---

### ðŸ’³ Payment Service (`Port 8083`)

**Database:** `payment_db` Â· **Security:** DTO Layer + IDOR protection + Admin-only refunds

| Method | Endpoint | Description | Auth | Role |
|---|---|---|---|:---:|
| `POST` | `/api/payments/process` | Process a payment | JWT Bearer | Any |
| `GET` | `/api/payments/{id}` | Get transaction by ID | JWT Bearer | Any |
| `GET` | `/api/payments/history/{userId}` | Get user payment history | JWT Bearer | Owner / Admin |
| `POST` | `/api/payments/refund/{id}` | Issue a refund | JWT Bearer | `ROLE_ADMIN` |

---

### ðŸ”” Notification Service (`Port 8085`)

**Database:** `notification_db` Â· **Security:** DTO-decoupled dispatches with Bean Validation

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/notifications/email` | Send an email notification | JWT Bearer |
| `POST` | `/api/notifications/sms` | Send an SMS notification | JWT Bearer |
| `GET` | `/api/notifications/user/{userId}` | Fetch user notification history | JWT Bearer |
| `GET` | `/api/notifications/{id}` | Fetch notification by ID | JWT Bearer |

---

## ðŸ§ª API Testing Guide

All requests route through the API Gateway at `http://localhost:8080`.

```mermaid
sequenceDiagram
    participant C as Tester
    participant GW as API Gateway :8080
    participant AUTH as Auth Service
    participant PROD as Product Service
    participant ORDER as Order Service
    participant PAY as Payment Service
    participant NOTIF as Notification Service

    C->>GW: 1. POST /api/auth/register
    GW->>AUTH: Forward (whitelist)
    AUTH-->>C: {token, username, role}

    C->>GW: 2. POST /api/auth/login
    GW->>AUTH: Forward (whitelist)
    AUTH-->>C: {token} â† save JWT

    C->>GW: 3. GET /api/products (Bearer JWT)
    GW->>PROD: Forward + X-API-KEY injected
    PROD-->>C: [product list]

    C->>GW: 4. POST /api/v1/orders (Bearer JWT)
    GW->>ORDER: Forward + X-API-KEY injected
    ORDER-->>C: {orderId, orderNumber, status: PENDING}

    C->>GW: 5. POST /api/payments/process (Bearer JWT)
    GW->>PAY: Forward + X-API-KEY injected
    PAY-->>C: {transactionId, status: COMPLETED}

    C->>GW: 6. POST /api/v1/orders/{id}/payment-webhook + X-Signature-SHA256
    GW->>ORDER: HMAC signature verified
    ORDER-->>C: {orderStatus: CONFIRMED}

    C->>GW: 7. POST /api/notifications/email (Bearer JWT)
    GW->>NOTIF: Forward + X-API-KEY injected
    NOTIF-->>C: {type: EMAIL, status: SENT}
```

### Step 1 â€” Register & Login

```bash
# Register an admin user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice_admin","password":"AdminPass123!","email":"alice@example.com","role":"ROLE_ADMIN"}'

# Login and save the returned token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice_admin","password":"AdminPass123!"}'
```

### Step 2 â€” Browse Products & Place Order

```bash
# List products
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer <YOUR_JWT>"

# Place an order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <YOUR_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "alice_admin",
    "customerEmail": "alice@example.com",
    "customerPhone": "+94771234567",
    "shippingAddress": {
      "street": "123 Main Street", "city": "Colombo",
      "state": "Western", "zipCode": "00300", "country": "Sri Lanka"
    },
    "items": [{"productId":"PROD-101","productName":"Wireless Headphones","unitPrice":150.00,"quantity":2}]
  }'
```

### Step 3 â€” Payment Webhook (HMAC-SHA256)

```bash
# Compute HMAC-SHA256 signature
BODY='{"paymentTransactionId":"TXN-A1B2C3D4","paymentStatus":"PAID","amount":300.00,"paymentGateway":"STRIPE","note":"Approved"}'
SIGNATURE=$(echo -n "$BODY" | openssl dgst -sha256 -hmac "your-webhook-secret" | awk '{print $2}')

# Send the signed webhook
curl -X POST http://localhost:8080/api/v1/orders/<ORDER_ID>/payment-webhook \
  -H "Content-Type: application/json" \
  -H "X-Signature-SHA256: $SIGNATURE" \
  -d "$BODY"
```

### Error Reference

| Status | Cause | Response |
|:---:|---|---|
| `400` | Validation error | `{"status":400,"error":"Validation Failed"}` |
| `401` | Missing / invalid JWT | `{"error":"Invalid or Expired JWT Token","status":401}` |
| `401` | Bad API key | `Unauthorized: Invalid or missing API Key` |
| `401` | Invalid webhook signature | `{"error":"Invalid webhook signature","status":401}` |
| `403` | Insufficient role | `Access denied: Admin role required` |
| `404` | Resource not found | `{"error":"Order not found","status":404}` |
| `429` | Rate limit exceeded (>60/min) | `{"error":"Too Many Requests","status":429}` |
| `500` | Unexpected server error | `{"error":"An unexpected error occurred","status":500}` |

> For the complete **15-step end-to-end walkthrough** (SMS, delivery tracking, refund, notification history), see the [API Testing Guide](docs/guides/api-testing.md).  
> A **Postman collection** is available at [`docs/postman/Order_Service.postman_collection.json`](docs/postman/Order_Service.postman_collection.json).

---

## ðŸ“ Project Structure

```
SOC/
â”œâ”€â”€ api-gateway/              # Spring Cloud Gateway â€” JWT, rate limiting, routing, CORS
â”œâ”€â”€ auth-service/             # Authentication â€” JWT issuance, BCrypt, user management
â”œâ”€â”€ product-service/          # Product catalog CRUD with ROLE_ADMIN RBAC
â”œâ”€â”€ order-service/            # Order lifecycle, HMAC webhooks, BOLA/IDOR protection
â”œâ”€â”€ payment-service/          # Payment processing, refunds, IDOR protection
â”œâ”€â”€ notification-service/     # Email & SMS notification dispatch
â”œâ”€â”€ client-app/               # Static frontend served via Nginx (port 3000)
â”œâ”€â”€ docs/
â”‚   â”œâ”€â”€ architecture/
â”‚   â”‚   â”œâ”€â”€ overview.md       # System architecture, design patterns, tech stack
â”‚   â”‚   â”œâ”€â”€ security.md       # JWT flow, HMAC, RBAC, rate limiting, filter chain
â”‚   â”‚   â””â”€â”€ data-models.md    # MongoDB collections and document schemas
â”‚   â”œâ”€â”€ guides/
â”‚   â”‚   â”œâ”€â”€ deployment.md     # Docker Compose, local Maven, CI/CD, troubleshooting
â”‚   â”‚   â””â”€â”€ api-testing.md    # 15-step end-to-end cURL testing guide
â”‚   â”œâ”€â”€ services/             # Per-service detailed API documentation
â”‚   â”œâ”€â”€ postman/              # Postman collection for Order Service
â”‚   â”œâ”€â”€ security-audit-report.md   # Full vulnerability audit & risk matrix
â”‚   â””â”€â”€ remediation-plan.md        # Phased patching roadmap & verification commands
â”œâ”€â”€ scripts/                  # Helper build & test automation scripts
â”œâ”€â”€ docker-compose.yml        # Full ecosystem orchestration (11 containers)
â”œâ”€â”€ .env.example              # Environment variable template
â”œâ”€â”€ CONTRIBUTING.md
â””â”€â”€ README.md
```

---

## ðŸ“š Documentation

| Document | Description |
|---|---|
| [Architecture Overview](docs/architecture/overview.md) | System design, patterns, technology stack, port reference |
| [Security Architecture](docs/architecture/security.md) | JWT flow, HMAC webhooks, RBAC, rate limiting, filter chain, CORS |
| [Data Models](docs/architecture/data-models.md) | MongoDB collection schemas, ER diagram, sample documents |
| [Deployment Guide](docs/guides/deployment.md) | Docker Compose, local Maven dev, environment variables, troubleshooting |
| [API Testing Guide](docs/guides/api-testing.md) | Complete 15-step end-to-end cURL testing walkthrough |
| [API Gateway](docs/services/api-gateway.md) | Routing rules, JWT filter, anti-traversal, rate limiting, CORS |
| [Auth Service](docs/services/auth-service.md) | Registration, login, JWT token management, exception handling |
| [Product Service](docs/services/product-service.md) | Catalog CRUD, RBAC admin protection, constant-time key validation |
| [Order Service](docs/services/order-service.md) | Order lifecycle, HMAC webhooks, BOLA/IDOR protection, delivery tracking |
| [Payment Service](docs/services/payment-service.md) | Payment DTO layer, transaction processing, admin refunds |
| [Notification Service](docs/services/notification-service.md) | DTO-decoupled Email & SMS dispatch, notification logs |
| [Security Audit Report](docs/security-audit-report.md) | Full vulnerability audit, risk matrix, and findings |
| [Remediation Plan](docs/remediation-plan.md) | Phased patching roadmap, task checklist & verification commands |

---

## ðŸ¤ Contributing

We welcome contributions! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for our workflow, conventions, and standards:

- **Git Workflow** â€” GitHub Flow with PR-based merging; `main` is always deployable
- **Commit Convention** â€” [Conventional Commits](https://www.conventionalcommits.org/) (`feat`, `fix`, `security`, `docs`, `chore`)
- **Branch Naming** â€” `feat/short-desc`, `fix/issue-desc`, `security/patch-desc`
- **Code Standards** â€” Java 17, Bean Validation on all DTOs, `@Profile("!prod")` on seeders, no plaintext secrets

```bash
git clone https://github.com/mrawfulshadow/SOC_project.git
cd SOC_project
cp .env.example .env
docker compose up --build -d
```

---

## ðŸ“„ License

Developed as part of the **SOC (Service-Oriented Computing)** course project.  
All contributions are subject to the project's academic integrity guidelines.

---

<div align="center">

**Built with Java 17 Â· Spring Boot 3.2.3 Â· Spring Cloud Gateway Â· MongoDB 7.0 Â· Docker**

</div>
