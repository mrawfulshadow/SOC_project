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
```
