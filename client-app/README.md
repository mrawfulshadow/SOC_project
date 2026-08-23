# 🖥️ Client App — Frontend

Static HTML/CSS/JS frontend for the **SOC Online E-Commerce & Delivery System**, served via **Nginx Alpine**.

---

## 📐 Architecture

```
client-app/
├── Dockerfile       ← nginx:alpine — copies index.html and serves on port 80
└── index.html       ← Single-page application (HTML + embedded JS/CSS)
```

The frontend communicates exclusively through the **API Gateway** at `http://localhost:8080`. It never calls individual microservices directly.

---

## 🚀 Running Locally

### Option A — Via Docker Compose (recommended)
Start the entire stack from the repo root:
```bash
docker compose up --build -d client-app
```
Then open: **http://localhost:3000**

### Option B — Standalone Docker build
```bash
# From the repo root
docker build -t soc/client-app ./client-app
docker run -p 3000:80 soc/client-app
```
Then open: **http://localhost:3000**

### Option C — Direct browser (no Docker)
Open `index.html` directly in your browser for a quick preview.  
> ⚠️ API calls will fail unless the gateway is running separately.

---

## 🌐 Access

| Environment | URL |
|---|---|
| Docker Compose | http://localhost:3000 |
| Standalone container | http://localhost:3000 |
| Direct file | `file:///path/to/client-app/index.html` |

---

## 🔧 API Gateway Integration

All REST calls are routed through:
```
http://localhost:8080
```

| Feature | Gateway Route |
|---|---|
| Login / Register | `POST /api/auth/login` · `POST /api/auth/register` |
| Browse Products | `GET /api/products` |
| Place Order | `POST /api/v1/orders` |
| Payment | `POST /api/payments/process` |
| Notifications | `GET /api/notifications/user/{userId}` |

---

## 🐳 Dockerfile Reference

```dockerfile
FROM nginx:alpine
COPY index.html /usr/share/nginx/html/index.html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

The image is minimal (~10 MB) and serves the SPA on container port `80`, mapped to host port `3000` by Docker Compose.

---

## 🔒 CORS Policy

The API Gateway enforces strict CORS, allowing requests only from:
- `http://localhost:3000`
- `http://127.0.0.1:3000`

Any other origin will be rejected with a `403 Forbidden`.
