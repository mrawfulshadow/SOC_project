# Deployment Guide

## Prerequisites

Ensure the following are installed on your machine before proceeding:

| Tool | Version | Purpose |
|---|---|---|
| Docker Desktop | 24+ | Container runtime and orchestration |
| Java JDK | 17+ | Required for local development |
| Maven | 3.8+ | Build tool (or use included `mvnw` wrapper) |
| Git | any | Source code management |
| MongoDB Compass | any | Optional — GUI for inspecting databases |

---

## Configuration Setup

Before starting any services, configure your environment secrets:

```bash
# Copy the example env file
cp .env.example .env

# Edit .env with your actual secrets
# Required values:
# JWT_SECRET=<base64-encoded-hmac-key>
# PRODUCT_SERVICE_API_KEY=<your-product-key>
# PAYMENT_SERVICE_API_KEY=<your-payment-key>
# ORDER_SERVICE_API_KEY=<your-order-key>
# NOTIFICATION_SERVICE_API_KEY=<your-notification-key>
# WEBHOOK_SECRET=<your-webhook-shared-secret>
# MONGO_ROOT_USERNAME=admin
# MONGO_ROOT_PASSWORD=<strong-password>
```

> **Production:** Set `SPRING_PROFILES_ACTIVE=prod` to prevent `DataInitializer` from seeding default accounts.

---

## Option 1: Docker Compose (Recommended)

This is the fastest way to run the full platform. Docker Compose starts all 11 containers (6 services + 5 MongoDB instances) within the isolated `soc-internal-net` bridge network, with no database ports exposed to the host.

### Start All Services

```bash
# From the project root (where docker-compose.yml is located)
docker compose up --build -d
```

Flags:
- `--build` — rebuilds all Docker images from source
- `-d` — runs in detached (background) mode

### Verify Services Are Running

```bash
docker compose ps
```

All containers should show `Up` status.

### View Logs

```bash
# Stream all service logs
docker compose logs -f

# Stream logs for a specific service
docker compose logs -f api-gateway
docker compose logs -f order-service
docker compose logs -f auth-service
```

### Stop Services

```bash
# Stop all services (preserves data volumes)
docker compose down

# Stop all services AND remove all persistent data
docker compose down -v
```

### Rebuild a Single Service

```bash
# Rebuild and restart only the API Gateway
docker compose up --build -d api-gateway
```

---

## Docker Compose Architecture

```mermaid
graph TB
    subgraph DockerCompose["docker-compose.yml — Isolated Internal Network (soc-internal-net)"]
        GW["api-gateway\n:8080 (host exposed)"]

        AS["auth-service\n:8084"]
        AM[("auth-mongodb\n(internal only)")]

        PS["product-service\n:8081"]
        PM[("product-mongodb\n(internal only)")]

        OS["order-service\n:8082"]
        OM[("order-mongodb\n(internal only)")]

        PAY["payment-service\n:8083"]
        PAYM[("payment-mongodb\n(internal only)")]

        NS["notification-service\n:8085"]
        NM[("notification-mongodb\n(internal only)")]
    end

    GW -->|"depends_on"| AS & PS & OS & PAY & NS
    AS -->|"depends_on"| AM
    PS -->|"depends_on"| PM
    OS -->|"depends_on"| OM
    PAY -->|"depends_on"| PAYM
    NS -->|"depends_on"| NM
```

> Database containers are **not** accessible from the host machine — they communicate only within the `soc-internal-net` bridge network with root authentication enabled.

---

## Option 2: Local Development with Maven

Use this approach for debugging individual services or making code changes.

### Step 1: Start MongoDB Instances (Docker only)

```bash
# For local dev only — bind to localhost
docker run -d -p 27022:27017 --name auth-mongodb mongo:7.0
docker run -d -p 27018:27017 --name product-mongodb mongo:7.0
docker run -d -p 27019:27017 --name order-mongodb mongo:7.0
docker run -d -p 27020:27017 --name payment-mongodb mongo:7.0
docker run -d -p 27021:27017 --name notification-mongodb mongo:7.0
```

### Step 2: Run Each Service

Open separate terminals for each service. **Start the API Gateway last.**

```bash
# Terminal 1 - Auth Service
cd auth-service
./mvnw spring-boot:run

# Terminal 2 - Product Service
cd product-service
./mvnw spring-boot:run

# Terminal 3 - Order Service
cd order-service
./mvnw spring-boot:run

# Terminal 4 - Payment Service
cd payment-service
./mvnw spring-boot:run

# Terminal 5 - Notification Service
cd notification-service
./mvnw spring-boot:run

# Terminal 6 - API Gateway (start LAST)
cd api-gateway
./mvnw spring-boot:run
```

> **Windows users:** Use `mvnw.cmd` instead of `./mvnw`

---

## Running Automated Tests & Security Scans

```bash
# Run all unit and regression tests for a service
cd api-gateway
mvn clean test

# Run OWASP Dependency Vulnerability Check
mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=8

# CI/CD: GitHub Actions runs all of these automatically on push/PR
# See: .github/workflows/ci.yml
```

---

## Environment Variables

### API Gateway

| Variable | Description | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring profile (`prod` disables data seeders) | `default` |
| `JWT_SECRET` | BASE64 HMAC-SHA256 key | *(required in `.env`)* |
| `AUTH_SERVICE_URL` | Auth service base URL | `http://localhost:8084` |
| `PRODUCT_SERVICE_URL` | Product service base URL | `http://localhost:8081` |
| `ORDER_SERVICE_URL` | Order service base URL | `http://localhost:8082` |
| `PAYMENT_SERVICE_URL` | Payment service base URL | `http://localhost:8083` |
| `NOTIFICATION_SERVICE_URL` | Notification service base URL | `http://localhost:8085` |

### All Microservices

| Variable | Description | Example (Docker) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |
| `SPRING_DATA_MONGODB_URI` | MongoDB connection URI | `mongodb://admin:pass@auth-mongodb:27017/auth_db` |
| `WEBHOOK_SECRET` | Shared HMAC secret for payment webhooks | *(required in `.env`)* |

---

## Health Check & Verification

### Verify all containers

```bash
docker compose ps
```

### Test the API Gateway is responsive

```bash
curl -I "http://localhost:8080/api/auth/validate?token=test"
# Expected: HTTP/1.1 200 (returns false for invalid token — service is running)
```

### Test Order Service health actuator

```bash
curl http://localhost:8082/actuator/health
# Expected: {"status":"UP"}
```

### Swagger UI Links (direct service access)

| Service | Swagger UI |
|---|---|
| Product Service | http://localhost:8081/swagger-ui.html |
| Order Service | http://localhost:8082/swagger-ui.html |
| Payment Service | http://localhost:8083/swagger-ui.html |
| Notification Service | http://localhost:8085/swagger-ui.html |

---

## Troubleshooting

### Port Already in Use

```bash
# Windows — find process using a port
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

### JWT Validation Errors

All services share the same JWT secret. Verify `JWT_SECRET` in `.env` is consistent and matches what's configured in both `api-gateway` and `auth-service`.

### Rate Limit Errors (429)

If you receive many `429 Too Many Requests` responses during testing, wait 60 seconds for the rate limit window to reset. The limit is **60 requests per minute per IP address**.

### MongoDB Connection Refused

Ensure the MongoDB container for that service is running:
```bash
docker ps | grep mongodb

# Restart a stopped container
docker compose start auth-mongodb
```
