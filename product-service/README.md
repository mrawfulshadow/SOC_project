# 📦 Product Catalog Service

**Student 2** — Spring Boot microservice managing the product catalog with full **CRUD** operations, **RBAC** access control, and **constant-time API key** validation.

- **Port:** `8081`
- **Database:** MongoDB (`product_db`) — dedicated container
- **Package:** `com.soc.productservice`
- **Stack:** Java 17 · Spring Boot 3.2.3 · Spring Data MongoDB · Lombok · Springdoc OpenAPI

---

## 📐 Architecture

```
product-service/
├── src/main/java/com/soc/productservice/
│   ├── config/
│   │   ├── ApiKeyFilter.java       ← Constant-time X-API-KEY validation
│   │   └── DataInitializer.java    ← @Profile("!prod") seed data
│   ├── controller/
│   │   └── ProductController.java  ← REST endpoints
│   ├── exception/
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   ├── model/
│   │   └── Product.java
│   ├── repository/
│   │   └── ProductRepository.java
│   ├── service/
│   │   └── ProductService.java
│   └── ProductServiceApplication.java
├── src/main/resources/
│   └── application.properties
├── src/test/
│   └── ...
├── Dockerfile
└── pom.xml
```

---

## 🌐 REST API

All routes are accessed via the **API Gateway** at `http://localhost:8080`.

| Method | Endpoint | Description | Auth | Role |
|--------|----------|-------------|------|------|
| `GET` | `/api/products` | List all products | `X-API-KEY` / JWT | Any |
| `GET` | `/products/{id}` | Get product by ID | `X-API-KEY` / JWT | Any |
| `POST` | `/products` | Create new product | `X-API-KEY` / JWT | `ROLE_ADMIN` |
| `DELETE` | `/products/{id}` | Delete product | `X-API-KEY` / JWT | `ROLE_ADMIN` |

### Example — List all products
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Example — Create product (Admin only)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Wireless Headphones",
        "description": "Noise-cancelling over-ear headphones",
        "price": 299.99,
        "stock": 50
      }'
```

---

## 🔒 Security Model

| Layer | Mechanism |
|---|---|
| **API Key** | `X-API-KEY` header validated with `MessageDigest.isEqual()` (constant-time, prevents timing attacks) |
| **RBAC** | Write/delete operations restricted to `ROLE_ADMIN` (header injected by API Gateway) |
| **Input validation** | Bean Validation (`@NotBlank`, `@Positive`) on all request DTOs |
| **Data seeding** | `DataInitializer` annotated `@Profile("!prod")` — never runs in production |

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATA_MONGODB_URI` | `mongodb://localhost:27018/product_db` | MongoDB connection string |
| `server.port` | `8081` | Service port |

In Docker Compose, MongoDB URI is injected automatically. For local dev, start a MongoDB instance on port `27018` or override the variable.

---

## 🚀 Running Locally

### Via Docker Compose (recommended)
```bash
# From repo root
docker compose up --build -d product-service product-mongodb
```

### Standalone Maven
```bash
# Requires MongoDB running on localhost:27018
cd product-service
./mvnw spring-boot:run
```

---

## 🧪 Tests

```bash
cd product-service
./mvnw clean test
```

Test reports → `target/surefire-reports/`

### OWASP Dependency Scan
```bash
./mvnw org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=8
```

---

## 📖 API Documentation (Swagger UI)

When the service is running:
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8081/api-docs
