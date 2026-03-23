# Architecture

![Architecture Diagram](./architecture.png)

# Architecture

## System Design

This platform uses a domain-oriented microservices architecture to separate user management, catalog operations, cart state, orders, payments, and notifications into independently deployable Spring Boot applications.

### Core Components

- `api-gateway`: routes requests and performs JWT validation before traffic reaches downstream services
- `frontend`: lightweight storefront-style demo UI that talks to the gateway
- `user-service`: owns user credentials, password hashing, profile retrieval, and token generation
- `product-service`: owns product catalog data and inventory, with Redis caching for product list reads
- `cart-service`: stores user carts directly in Redis for low-latency read/write access
- `order-service`: persists orders and publishes checkout events
- `payment-service`: consumes order events, simulates payment processing, persists payment state, and emits payment completion events
- `notification-service`: consumes payment completion events and simulates user notifications

## Data Stores

- PostgreSQL
  - `user-service`
  - `product-service`
  - `order-service`
  - `payment-service`
- Redis
  - primary cart storage
  - product list caching
- Kafka
  - event backbone for the checkout workflow

## Request Flow

1. Client sends requests to `api-gateway`.
2. The optional frontend demo also talks only to the gateway.
3. Gateway validates JWT for all secured routes.
4. Gateway forwards requests to the appropriate downstream service.
5. Each service remains independently deployable and owns its own data.

## Event Flow

### Topics

- `order-created`
- `payment-completed`

### Checkout Sequence

1. A client sends `POST /api/orders`.
2. `order-service` stores the order in PostgreSQL.
3. `order-service` publishes an `OrderCreatedEvent` to `order-created`.
4. `payment-service` consumes the event, creates a payment record, marks it `COMPLETED`, and publishes `PaymentCompletedEvent` to `payment-completed`.
5. `notification-service` consumes the payment completion event and logs/stores a notification entry.

## Security

- JWT-based authentication
- `register` and `login` are public
- all other gateway routes require a bearer token
- downstream secured services validate the same token contract using a shared secret

## Scalability Considerations

- independent service deployment and scaling
- Redis for fast cart access and reducing product listing load
- Kafka decouples order submission from payment and notification processing
- database-per-service model limits cross-service schema coupling

## Operational Model

- each service includes a dedicated Dockerfile
- `docker-compose.yml` provisions infrastructure plus all microservices
- services communicate through container DNS names such as `user-service`, `kafka`, and `redis`
- each service exposes actuator health endpoints for runtime checks
- each HTTP service exposes Swagger/OpenAPI docs for quick exploration

## Observability And Developer Experience

- `Spring Boot Actuator` is enabled for health and info endpoints
- `Springdoc OpenAPI` is enabled for interactive API documentation
- structured application logging is enabled across services through Spring Boot logging

## Demo URLs

- `api-gateway`: `http://localhost:8080/actuator/health`
- `user-service`: `http://localhost:8081/swagger-ui.html`
- `product-service`: `http://localhost:8082/swagger-ui.html`
- `cart-service`: `http://localhost:8083/swagger-ui.html`
- `order-service`: `http://localhost:8084/swagger-ui.html`
- `payment-service`: `http://localhost:8085/swagger-ui.html`
- `notification-service`: `http://localhost:8086/swagger-ui.html`
- `frontend`: `http://localhost:3000`

