# Postman Collections

Import these JSON files into [Postman](https://www.postman.com/) to test all SOC microservice APIs end-to-end via the API Gateway.

## Available Collections

| Collection File | Service | Base URL |
|---|---|---|
| [`Order_Service.postman_collection.json`](./Order_Service.postman_collection.json) | Order Service | `http://localhost:8080/api/v1/orders` |

## How to Import

1. Open **Postman**
2. Click **Import** (top-left)
3. Drag & drop the `.json` file, or click **Upload Files** and select it
4. The collection will appear in your left sidebar

## Environment Setup

Before running requests, set these Postman environment variables:

| Variable | Example Value | Description |
|---|---|---|
| `base_url` | `http://localhost:8080` | API Gateway URL |
| `jwt_token` | _(from `/api/auth/login`)_ | Bearer token for authenticated requests |
| `webhook_secret` | _(from `.env`)_ | HMAC-SHA256 secret for webhook calls |

## Quick Auth Flow

1. **Register** → `POST {{base_url}}/api/auth/register`
2. **Login** → `POST {{base_url}}/api/auth/login` — copy the `token` from the response
3. **Set variable** → paste token into `jwt_token` environment variable
4. **Run any protected request** — Authorization header is pre-configured in each collection
