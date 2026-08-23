# 📚 SOC Microservices Documentation

> **Online E-Commerce & Delivery System** — Enterprise-grade, security-hardened microservices platform built with Java 17, Spring Boot 3, Spring Cloud Gateway, JWT, HMAC-SHA256, RBAC, and Docker.

---

## 📖 Documentation Index

| Document | Description |
|---|---|
| [Architecture Overview](architecture/overview.md) | System architecture, design patterns, and technology stack |
| [Security Architecture](architecture/security.md) | JWT flow, HMAC webhooks, RBAC, rate limiting, CORS, and filter chain |
| [Security & QA Audit Report](security-audit-report.md) | Comprehensive vulnerability audit, risk matrix & findings |
| [Security Patch Plan & Checklist](remediation-plan.md) | Phased patching roadmap, task checklist & verification commands |
| [Data Models](architecture/data-models.md) | MongoDB collections and document schemas |
| [API Gateway](services/api-gateway.md) | Routing, JWT filter, anti-traversal, rate limiting, CORS, API key injection |
| [Auth Service](services/auth-service.md) | Registration, login, JWT token management, validation, exception handling |
| [Product Service](services/product-service.md) | Catalog CRUD, RBAC admin protection, constant-time API key validation |
| [Order Service](services/order-service.md) | Order lifecycle, HMAC webhooks, BOLA/IDOR protection, delivery tracking |
| [Payment Service](services/payment-service.md) | Payment DTO layer, transaction processing, admin refunds, user history IDOR |
| [Notification Service](services/notification-service.md) | DTO-decoupled Email & SMS dispatch and notification logs |
| [Deployment Guide](guides/deployment.md) | Docker Compose, `.env` setup, local Maven, CI/CD, OWASP scans |
| [API Testing Guide](guides/api-testing.md) | End-to-end API testing with cURL including HMAC webhook signing |

---

## 🚀 Quick Start

```bash
# 1. Configure secrets
cp .env.example .env
# Edit .env with your JWT secret, API keys, webhook secret, and MongoDB credentials

# 2. Start all services
docker compose up --build -d

# 3. Run automated tests
mvn clean test

# 4. Run OWASP dependency vulnerability scans
mvn org.owasp:dependency-check-maven:check
```

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Auth Service | http://localhost:8084 |
| Product Service | http://localhost:8081 |
| Order Service | http://localhost:8082 |
| Payment Service | http://localhost:8083 |
| Notification Service | http://localhost:8085 |

---

## 🏗️ Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.x |
| API Gateway | Spring Cloud Gateway 2023.0.0 |
| Security | Spring Security + JJWT 0.11.5 (HMAC-SHA256) |
| Database | MongoDB 7.0 (isolated instance per service) |
| Containerization | Docker + Docker Compose 3.8 (private bridge network) |
| API Documentation | OpenAPI 3 / Swagger UI |
| Testing | JUnit 5, Mockito, WebFlux Test |
| Vulnerability Scanning | OWASP Dependency-Check Maven Plugin 9.0.9 |
| CI/CD | GitHub Actions |
| Build Tool | Maven (via Maven Wrapper) |

---

## 🛡️ Security Hardening Summary

All 14 security vulnerabilities identified in the [Security Audit Report](security-audit-report.md) have been resolved:

| Phase | Patch | Status |
|---|---|:---:|
| 1 | Database network isolation (no public ports) | ✅ |
| 1 | AntPathMatcher + traversal hardening in Gateway JWT filter | ✅ |
| 1 | Secrets externalized to `.env` + `@Value` injection | ✅ |
| 1 | Constant-time `MessageDigest.isEqual` API key validation | ✅ |
| 2 | `ROLE_ADMIN` RBAC on product write/delete and payment refunds | ✅ |
| 2 | BOLA/IDOR ownership checks on orders and payment history | ✅ |
| 2 | HMAC-SHA256 webhook signature verification | ✅ |
| 2 | CORS restricted to explicit trusted origins | ✅ |
| 2 | Rate limiting with trusted proxy subnet validation | ✅ |
| 3 | `@Profile("!prod")` on all data initializers | ✅ |
| 3 | DTO layer for Payment & Notification (anti-mass assignment) | ✅ |
| 3 | Centralized `GlobalExceptionHandler` across all services | ✅ |
| 4 | Unit & Mockito test suites for all services | ✅ |
| 4 | API Gateway security regression tests | ✅ |
| 4 | GitHub Actions CI/CD + OWASP Dependency-Check integration | ✅ |

---

*Developed as part of the SOC (Service-Oriented Computing) course project.*
