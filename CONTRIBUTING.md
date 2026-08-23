# Contributing to SOC Microservices

Thank you for contributing to the **SOC Online E-Commerce & Delivery System**! This guide defines our team workflow, conventions, and standards so every contributor can collaborate efficiently.

---

## 📋 Table of Contents

- [Project Structure](#project-structure)
- [Git Workflow](#git-workflow)
- [Commit Message Convention](#commit-message-convention)
- [Branch Naming](#branch-naming)
- [Pull Request Process](#pull-request-process)
- [Local Development Setup](#local-development-setup)
- [Running Tests](#running-tests)
- [Code Standards](#code-standards)

---

## 🗂️ Project Structure

```
SOC/
├── api-gateway/          # Spring Cloud Gateway — JWT, rate limiting, routing
├── auth-service/         # Authentication — JWT issuance, BCrypt, user management
├── product-service/      # Product catalog CRUD with RBAC
├── order-service/        # Order lifecycle, HMAC webhooks, BOLA protection
├── payment-service/      # Payment processing, refunds, IDOR protection
├── notification-service/ # Email & SMS notification dispatch
├── client-app/           # Static frontend served via Nginx
├── docs/                 # Architecture docs, API guides, Postman collections
├── scripts/              # Helper build & test scripts
├── docker-compose.yml    # Full ecosystem orchestration
├── .env.example          # Environment template (copy to .env and fill in)
└── .gitignore            # Repo-wide ignores
```

---

## 🌿 Git Workflow

We use **GitHub Flow**:

1. **`main`** is always deployable — never commit directly to it.
2. Create a feature/fix branch from `main`.
3. Open a Pull Request (PR) targeting `main`.
4. At least **one reviewer** must approve before merging.
5. Squash-merge into `main` after approval.

---

## ✍️ Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

### Types

| Type | When to use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `refactor` | Code restructuring (no behavior change) |
| `test` | Adding or updating tests |
| `chore` | Build process, CI, dependencies |
| `security` | Security-related fixes or hardening |

### Examples

```
feat(order-service): add HMAC-SHA256 webhook signature validation
fix(api-gateway): prevent path traversal bypass via semicolons
security(auth-service): enforce BCrypt cost factor of 12
docs(readme): update API endpoint table for payment service
chore(ci): add Docker build verification step
```

---

## 🌿 Branch Naming

```
<type>/<short-description>

feat/payment-refund-rbac
fix/jwt-expiry-clock-skew
docs/postman-collection-update
chore/root-gitignore
security/rate-limit-bypass-patch
```

---

## 🔃 Pull Request Process

1. **Title** — Follow the commit convention format.
2. **Description** — Explain *what* changed and *why*.
3. **Checklist before opening PR:**
   - [ ] `mvn clean test` passes locally for your service
   - [ ] No secrets or credentials in the diff
   - [ ] `target/` directories are NOT in the diff (covered by `.gitignore`)
   - [ ] Docker image builds successfully: `docker build -t test .`
4. Link any related issues in the PR description.
5. Assign the relevant **CODEOWNER** as reviewer (see `.github/CODEOWNERS`).

---

## 💻 Local Development Setup

### Prerequisites

| Tool | Version |
|------|---------|
| JDK | 17 (Temurin recommended) |
| Maven | 3.9+ (or use included `./mvnw`) |
| Docker | 24+ |
| Docker Compose | v2+ |

### Quick Start

```bash
# 1. Clone the repo
git clone <repo-url>
cd SOC

# 2. Set up environment
cp .env.example .env
# Edit .env and fill in strong secrets

# 3. Start all services
docker compose up --build -d

# 4. Verify all containers are running
docker compose ps
```

### Service URLs (local)

| Service | URL |
|---------|-----|
| Client App | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Auth Service | http://localhost:8084 |
| Product Service | http://localhost:8081 |
| Order Service | http://localhost:8082 |
| Payment Service | http://localhost:8083 |
| Notification Service | http://localhost:8085 |

---

## 🧪 Running Tests

### Single service
```bash
cd <service-name>
./mvnw clean test
```

### All services (PowerShell)
```powershell
foreach ($svc in @("api-gateway","auth-service","product-service","order-service","payment-service","notification-service")) {
    Write-Host "Testing $svc..." -ForegroundColor Cyan
    Push-Location $svc; .\mvnw clean test; Pop-Location
}
```

### All services (Bash/Linux/macOS)
```bash
bash scripts/test-all.sh
```

### OWASP Dependency Vulnerability Scan
```bash
cd <service-name>
./mvnw org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=8
```

---

## 📐 Code Standards

### Java
- Use **Java 17** features where appropriate (records, sealed classes).
- All REST endpoints must have **Bean Validation** annotations on DTOs.
- Use `@Profile("!prod")` on any data-seeding initializers.
- No plaintext secrets in source code — use environment variables.
- All public methods in service classes must have Javadoc.

### Security
- Never bypass the API Gateway for inter-service calls in production.
- All write/delete endpoints must check ownership or `ROLE_ADMIN`.
- Validate and sanitize all user-supplied inputs at the DTO layer.
- Never log sensitive data (passwords, tokens, API keys).

### Git
- Never commit: `.env`, `target/`, `*.log`, IDE config files.
- Keep PRs focused — one feature or fix per PR.
- Rebase your branch on `main` before requesting review.

---

## 📜 License

This project is developed as part of the **SOC (Service-Oriented Computing)** course. All contributions are subject to the project's academic integrity guidelines.
