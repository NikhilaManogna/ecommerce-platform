# API Documentation

All examples assume the API Gateway base URL:

```text
http://localhost:8080
```

Demo frontend:

```text
http://localhost:3000
```

Use:

```text
Authorization: Bearer <jwt-token>
```

for all secured endpoints.

## Interactive Docs

- User Service Swagger: `http://localhost:8081/swagger-ui.html`
- Product Service Swagger: `http://localhost:8082/swagger-ui.html`
- Cart Service Swagger: `http://localhost:8083/swagger-ui.html`
- Order Service Swagger: `http://localhost:8084/swagger-ui.html`
- Payment Service Swagger: `http://localhost:8085/swagger-ui.html`
- Notification Service Swagger: `http://localhost:8086/swagger-ui.html`

## Health Endpoints

- Gateway: `http://localhost:8080/actuator/health`
- User Service: `http://localhost:8081/actuator/health`
- Product Service: `http://localhost:8082/actuator/health`
- Cart Service: `http://localhost:8083/actuator/health`
- Order Service: `http://localhost:8084/actuator/health`
- Payment Service: `http://localhost:8085/actuator/health`
- Notification Service: `http://localhost:8086/actuator/health`

## User Service

### Register

- `POST /api/users/register`

Request:

```json
{
  "name": "abc",
  "email": "abc@example.com",
  "password": "password123"
}
```

### Login

- `POST /api/users/login`

Request:

```json
{
  "email": "abc@example.com",
  "password": "password123"
}
```

### Get Profile

- `GET /api/users/profile`

## Product Service

### Create Product

- `POST /api/products`

```json
{
  "name": "Mechanical Keyboard",
  "description": "Compact hot-swappable keyboard",
  "price": 99.99,
  "stock": 20,
  "category": "Accessories"
}
```

### List Products

- `GET /api/products`

### Get Product

- `GET /api/products/{id}`

### Update Product

- `PUT /api/products/{id}`

### Update Inventory

- `PATCH /api/products/{id}/inventory?stock=25`

### Delete Product

- `DELETE /api/products/{id}`

## Cart Service

### Add Item

- `POST /api/cart/items`

```json
{
  "productId": 1,
  "productName": "Mechanical Keyboard",
  "price": 99.99,
  "quantity": 2
}
```

### Update Item Quantity

- `PUT /api/cart/items/{productId}`

```json
{
  "quantity": 3
}
```

### Remove Item

- `DELETE /api/cart/items/{productId}`

### Get Cart

- `GET /api/cart`

## Order Service

### Create Order

- `POST /api/orders`

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 99.99
    }
  ]
}
```

### Get Order History

- `GET /api/orders`

### Update Order Status

- `PATCH /api/orders/{orderId}/status`

```json
{
  "status": "PAID"
}
```

## Payment Service

### Get Payment By Order

- `GET /api/payments/order/{orderId}`

## Notification Service

### List Notifications

- `GET /api/notifications`

## Gateway Notes

- All client traffic should go through http://localhost:8080`r
- The API Gateway centralizes request routing and JWT validation
- Kafka flow for checkout: Order Service -> order-created -> Payment Service -> payment-completed -> Notification Service`r

