# Order Service - E-Commerce & Delivery Platform

This repository contains the **Order Service** backend module for an online E-commerce and delivery system. It handles everything from order placement and total price calculation to payment callbacks, status tracking, and delivery updates.

---

## What This Service Does

- **Order Placement**: Accepts customer order items, calculates item subtotals and grand total, and generates a unique order reference number (e.g. `ORD-20260816-A1B2C3`).
- **Status Lifecycle Tracking**: Manages order progression through `PENDING` -> `CONFIRMED` -> `PROCESSING` -> `OUT_FOR_DELIVERY` -> `DELIVERED` (or `CANCELLED`).
- **Payment Webhook Handling**: Receives callbacks from payment gateways (`PAID` or `FAILED`) and updates the order status automatically upon payment confirmation.
- **Delivery Dispatch Sync**: Stores courier tracking numbers, assigned carrier details, estimated delivery time, and dispatch notes.
- **REST APIs & Docs**: Exposes clean JSON endpoints documented with interactive Swagger UI (`/swagger-ui.html`).

---

## Tech Stack

- **Language & Framework**: Java 17, Spring Boot 3
- **Database**: MongoDB 7.0
- **Documentation**: OpenAPI 3 / Swagger UI
- **Containerization**: Docker & Docker Compose
- **Testing**: JUnit 5, Mockito, Postman Collection

---

## Project Structure

```text
order-service/
├── Dockerfile                               # Multi-stage container build
├── docker-compose.yml                       # Starts app, MongoDB, and Mongo Express GUI
├── Order_Service.postman_collection.json    # Postman test collection
├── pom.xml                                  # Maven dependencies
├── README.md                                # Project documentation
└── src/
    ├── main/java/com/ecommerce/orderservice/
    │   ├── OrderServiceApplication.java     # Entry point
    │   ├── config/                          # Swagger configuration
    │   ├── controller/                      # REST API endpoints
    │   ├── dto/                             # Request and response models
    │   ├── exception/                       # Global exception handling
    │   ├── model/                           # MongoDB documents & enums
    │   ├── repository/                      # Spring Data MongoDB repository
    │   └── service/                         # Order business logic
    └── main/resources/
        └── application.yml                  # Config settings
```

---

## How to Run

### Option 1: Docker (Easiest)

Make sure Docker Desktop is running, then start all services:

```bash
docker compose up --build -d
```

Once running, access:
- **Order Service API**: `http://localhost:8081`
- **Swagger Documentation**: `http://localhost:8081/swagger-ui.html`
- **Mongo Express GUI**: `http://localhost:8082`

To stop:
```bash
docker compose down
```

---

### Option 2: Run Locally with Maven

```bash
# 1. Start MongoDB only
docker compose up -d mongodb

# 2. Run the Spring Boot application
mvn spring-boot:run
```

---

## Main API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/orders` | Place a new order |
| `GET` | `/api/v1/orders/{id}` | Get order details by ID or Order Number |
| `GET` | `/api/v1/orders` | Get all orders (supports pagination & status filter) |
| `PATCH` | `/api/v1/orders/{id}/status` | Update order status |
| `POST` | `/api/v1/orders/{id}/payment-webhook` | Receive payment gateway webhook |
| `PATCH` | `/api/v1/orders/{id}/delivery` | Update courier dispatch & tracking details |
| `DELETE` | `/api/v1/orders/{id}` | Cancel an order |
| `GET` | `/actuator/health` | Health check endpoint |

---

## Sample Request Payloads

### Place Order (`POST /api/v1/orders`)
```json
{
  "customerId": "CUST-1001",
  "customerEmail": "user@example.com",
  "customerPhone": "+94771234567",
  "shippingAddress": {
    "street": "123 Main Street",
    "city": "Colombo",
    "state": "Western",
    "zipCode": "00300",
    "country": "Sri Lanka"
  },
  "items": [
    {
      "productId": "PROD-101",
      "productName": "Wireless Headphones",
      "unitPrice": 150.00,
      "quantity": 2
    }
  ]
}
```

### Payment Callback (`POST /api/v1/orders/{id}/payment-webhook`)
```json
{
  "transactionId": "TXN-987654321",
  "paymentStatus": "PAID",
  "amountPaid": 300.00,
  "paymentMethod": "CREDIT_CARD",
  "gatewayResponseMessage": "Approved successfully"
}
```

### Update Delivery Info (`PATCH /api/v1/orders/{id}/delivery`)
```json
{
  "deliveryStatus": "IN_TRANSIT",
  "carrier": "DHL Express",
  "trackingNumber": "DHL-SL-998822",
  "estimatedDeliveryTime": "2026-08-18T17:00:00",
  "dispatchNotes": "Picked up by rider"
}
```

---

## Testing with Postman

1. Open Postman.
2. Click **Import** and select `Order_Service.postman_collection.json`.
3. Set `baseUrl` to `http://localhost:8081`.
4. Run requests in numbered order. Placing a new order automatically saves the `orderId` variable for subsequent requests.
