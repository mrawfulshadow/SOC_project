# Notification Service

## Overview

The Notification Service dispatches and logs Email and SMS notifications triggered by order and payment events. The service decouples API payload structures using dedicated Request/Response DTOs, validates input contracts via Jakarta Bean Validation, and centralizes error handling with `@RestControllerAdvice`.

- **Port:** `8085`
- **Database:** `notification_db` (MongoDB inside `soc-internal-net`)
- **Collection:** `notifications`
- **Package:** `com.soc.notificationservice`
- **Security:** `X-API-KEY: notification-secret-key-123` (Constant-Time Verification)

---

## Class Diagram

```mermaid
classDiagram
    class Notification {
        +String id
        +Long userId
        +Long orderId
        +String recipient
        +String type
        +String message
        +String status
        +LocalDateTime createdAt
    }

    class NotificationRequestDTO {
        +Long userId
        +Long orderId
        +String recipient
        +String message
    }

    class NotificationResponseDTO {
        +String id
        +Long userId
        +Long orderId
        +String recipient
        +String type
        +String message
        +String status
        +LocalDateTime createdAt
    }

    class NotificationRepository {
        +findByUserId(Long) List~Notification~
        +findById(String) Optional~Notification~
        +save(Notification) Notification
    }

    class NotificationController {
        -NotificationRepository repository
        +POST /api/notifications/email
        +POST /api/notifications/sms
        +GET /api/notifications/user/{userId}
        +GET /api/notifications/{id}
    }

    class ApiKeyFilter {
        -validApiKey: String
        +doFilter(request, response, chain) void
    }

    NotificationController --> NotificationRepository
    NotificationController ..> NotificationRequestDTO
    NotificationController ..> NotificationResponseDTO
    NotificationRepository --> Notification
    ApiKeyFilter ..> NotificationController : "guards"
```

---

## REST API Endpoints

| Method | Endpoint | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/notifications/email` | Send an email notification | `NotificationRequestDTO` | `201 Created` — `NotificationResponseDTO` |
| `POST` | `/api/notifications/sms` | Send an SMS notification | `NotificationRequestDTO` | `201 Created` — `NotificationResponseDTO` |
| `GET` | `/api/notifications/user/{userId}` | Get all notifications for a user | None | `200 OK` — `List<NotificationResponseDTO>` |
| `GET` | `/api/notifications/{id}` | Get notification by ID | None | `200 OK` — `NotificationResponseDTO` |

---

## Sample Request & Response

### POST `/api/notifications/email`

**Request:**
```json
{
  "userId": 1,
  "orderId": 1001,
  "recipient": "john@example.com",
  "message": "Your order ORD-20260822-A1B2C3 has been confirmed and is being processed."
}
```

**Response (`201 Created`):**
```json
{
  "id": "64e5f6001234567890abcd",
  "userId": 1,
  "orderId": 1001,
  "recipient": "john@example.com",
  "type": "EMAIL",
  "message": "Your order ORD-20260822-A1B2C3 has been confirmed and is being processed.",
  "status": "SENT",
  "createdAt": "2026-08-22T22:20:00"
}
```

---

## Swagger / OpenAPI

- **Swagger UI:** `http://localhost:8085/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8085/v3/api-docs`
- **Security Scheme:** `ApiKeyAuth` (header `X-API-KEY`)
