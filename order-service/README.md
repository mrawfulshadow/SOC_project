# Order Service — E-Commerce & Delivery Platform

This module is the **Order Service** backend for the SOC Online E-Commerce and Delivery System. It handles order placement, lifecycle management, HMAC-SHA256-verified payment callbacks, BOLA/IDOR-protected reads, and delivery tracking updates.

---

## What This Service Does

- **Order Placement**: Accepts customer order items, calculates item subtotals and grand total, and generates a unique order reference number (e.g. `ORD-20260816-A1B2C3`).
- **Ownership-Protected Reads**: Enforces customer identity matching (`X-User-Name` header vs `customerId`) on get/delete operations — prevents Broken Object-Level Authorization (BOLA/IDOR).
- **Status Lifecycle Tracking**: Manages order progression through `PENDING` → `CONFIRMED` → `PROCESSING` → `OUT_FOR_DELIVERY` → `DELIVERED` (or `CANCELLED`).
- **HMAC-SHA256 Webhook Verification**: Validates the `X-Signature-SHA256` header on payment callbacks — forged or missing signatures are rejected with `401 Unauthorized`.
- **Delivery Dispatch Sync**: Stores courier tracking numbers, assigned carrier details, estimated delivery time, and dispatch notes.
- **REST APIs & Docs**: Clean JSON endpoints documented with interactive Swagger UI (`/swagger-ui.html`).

---

## Tech Stack

- **Language & Framework**: Java 17, Spring Boot 3
- **Database**: MongoDB 7.0 (internal Docker network, no public port exposure)
- **Documentation**: OpenAPI 3 / Swagger UI
- **Containerization**: Docker & Docker Compose (`soc-internal-net` bridge network)
- **Testing**: JUnit 5, Mockito, Postman Collection
- **Security**: `InvalidWebhookSignatureException`, `GlobalExceptionHandler`, Jakarta Bean Validation

---

## Project Structure

```text
order-service/
├── Dockerfile                               # Multi-stage container build
├── pom.xml                                  # Maven dependencies + OWASP plugin
├── README.md                                # Project documentation
└── src/
    ├── main/java/com/ecommerce/orderservice/
    │   ├── OrderServiceApplication.java     # Entry point
    │   ├── config/                          # Swagger configuration
    │   ├── controller/                      # REST API endpoints
    │   ├── dto/                             # Request and response models
    │   ├── exception/                       # GlobalExceptionHandler, custom exceptions
    │   ├── model/                           # MongoDB documents & enums
    │   ├── repository/                      # Spring Data MongoDB repository
    │   └── service/                         # Order business logic (HMAC verification)
    └── main/resources/
        └── application.yml                  # Config settings (webhook.secret from .env)
```

---

## How to Run

### Via Docker Compose (from project root)

```bash
# Copy and configure environment secrets first
cp .env.example .env

# Start all services
docker compose up --build -d

# View order service logs
docker compose logs -f order-service
```

Access:
- **Swagger UI**: `http://localhost:8082/swagger-ui.html`
- **Health Check**: `http://localhost:8082/actuator/health`

### Locally with Maven

```bash
# Start a local MongoDB on port 27019
docker run -d -p 27019:27017 --name order-mongodb mongo:7.0

# Run the Spring Boot service
mvn spring-boot:run
```

---

## API Endpoints

| Method | Endpoint | Description | Authorization |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/orders` | Place a new order | JWT Bearer |
| `GET` | `/api/v1/orders/{id}` | Get order by ID/Number (owner only) | JWT Bearer + Ownership |
| `GET` | `/api/v1/orders` | List orders (own or all if Admin) | JWT Bearer |
| `PATCH` | `/api/v1/orders/{id}/status` | Update order status | JWT Bearer |
| `POST` | `/api/v1/orders/{id}/payment-webhook` | Receive payment webhook | `X-Signature-SHA256` |
| `PATCH` | `/api/v1/orders/{id}/delivery` | Update courier tracking details | JWT Bearer |
| `DELETE` | `/api/v1/orders/{id}` | Cancel order (owner or Admin) | JWT Bearer |
| `GET` | `/actuator/health` | Health check | None |

---

## Payment Webhook — HMAC Signature

All requests to `POST /api/v1/orders/{id}/payment-webhook` must include the `X-Signature-SHA256` header — an HMAC-SHA256 hex digest of the raw JSON request body signed with the shared `WEBHOOK_SECRET`.

**Compute signature example (bash):**
```bash
BODY='{"paymentTransactionId":"TXN-XYZ","paymentStatus":"PAID","amount":300.00}'
SIG=$(echo -n "$BODY" | openssl dgst -sha256 -hmac "your-webhook-secret" | awk '{print $2}')

curl -X POST http://localhost:8082/api/v1/orders/<ORDER_ID>/payment-webhook \
  -H "Content-Type: application/json" \
  -H "X-Signature-SHA256: $SIG" \
  -d "$BODY"
```

Missing or invalid signatures return **`401 Unauthorized`**.

---

## Testing

```bash
# Run all unit tests
mvn clean test

# Run OWASP dependency vulnerability scan
mvn org.owasp:dependency-check-maven:check
```

**Postman:**
1. Import `Order_Service.postman_collection.json`.
2. Set `baseUrl` = `http://localhost:8082`.
3. Run requests in numbered order — placing an order auto-saves `orderId`.
