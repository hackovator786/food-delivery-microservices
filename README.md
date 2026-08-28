# Food Delivery Microservices

A production-ready, full-featured food delivery platform built with **Java 21**, **Spring Boot 3.5.3**, and **Spring Cloud 2025.0.0**, following microservices architecture principles.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Services](#services)
  - [Service Registry](#1-service-registry)
  - [API Gateway](#2-api-gateway)
  - [Auth Service](#3-auth-service)
  - [User Service](#4-user-service)
  - [Email Service](#5-email-service)
  - [Restaurant Service](#6-restaurant-service)
  - [Menu Service](#7-menu-service)
  - [Search Service](#8-search-service)
  - [Cart Service](#9-cart-service)
  - [Order Service](#10-order-service)
  - [Payment Service](#11-payment-service)
  - [Delivery Service](#12-delivery-service)
- [Technology Stack](#technology-stack)
- [Infrastructure](#infrastructure)
- [Kafka Event Bus](#kafka-event-bus)
- [Inter-Service Communication](#inter-service-communication)
- [Security & Authentication](#security--authentication)
- [Database Schema](#database-schema)
- [External Integrations](#external-integrations)
- [Environment Variables](#environment-variables)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)

---

## Architecture Overview

```
                        ┌──────────────────────────────────────────────┐
                        │              CLIENT (Web / Mobile)            │
                        └─────────────────────┬────────────────────────┘
                                               │
                        ┌──────────────────────▼────────────────────────┐
                        │       API GATEWAY  (port 9000)                │
                        │  JWT Validation · Rate Limiting · Routing     │
                        └──┬──────┬──────┬──────┬──────┬──────┬────────┘
                           │      │      │      │      │      │
              ┌────────────▼─┐ ┌──▼──┐ ┌▼────┐ ┌▼────┐ ┌▼────┐ ┌▼────────┐
              │ Auth Service │ │User │ │Rest.│ │Menu │ │Cart │ │ Order  │
              │    (8081)    │ │(8082│ │(8083│ │(8084│ │(8085│ │ (8092) │
              └──────────────┘ └─────┘ └─────┘ └──┬──┘ └─────┘ └───┬────┘
                                                   │ Kafka           │
                      ┌──────────────────────────── │ ───────────────┼───┐
                      │           KAFKA BUS         │               │   │
                      └──────────────────────────── │ ───────────────┼───┘
                                                   │                │
                              ┌────────────────────▼──┐  ┌──────────▼──────┐
                              │   Search Service (8086)│  │Payment Svc (8093)│
                              │     Elasticsearch      │  │  Razorpay/Stripe │
                              └───────────────────────┘  └──────────────────┘
                                                                  │
                                             ┌────────────────────▼──┐
                                             │  Delivery Svc (8091)  │
                                             └───────────────────────┘

                        ┌─────────────────────────────────────┐
                        │  SERVICE REGISTRY / EUREKA (8761)   │
                        │  All services self-register here     │
                        └─────────────────────────────────────┘
```

---

## Services

### 1. Service Registry

| Property | Value |
|----------|-------|
| **Port** | `8761` |
| **Technology** | Netflix Eureka Server |
| **Role** | Central service discovery and registration |

All microservices register with this Eureka server on startup. The API Gateway resolves service instances by logical name through Eureka, enabling client-side load balancing.

---

### 2. API Gateway

| Property | Value |
|----------|-------|
| **Port** | `9000` |
| **Technology** | Spring Cloud Gateway (WebFlux), Redis |
| **Role** | Single entry point — routing, JWT validation, rate limiting |

**Routing Table**

| Path Prefix | Upstream Service |
|-------------|-----------------|
| `/auth/**` | AUTH-SERVICE |
| `/user/**` | USER-SERVICE |
| `/restaurant/**` | RESTAURANT-SERVICE |
| `/menu/**` | MENU-SERVICE |
| `/search/**` | SEARCH-SERVICE |
| `/cart/**` | CART-SERVICE |
| `/order/**` | ORDER-SERVICE |
| `/payment/**` | PAYMENT-SERVICE |

**Rate Limiting (Redis-backed, per route)**

| Route | Limit |
|-------|-------|
| OTP send (signup/login) | 15 req / 60 s |
| OTP verify | 10 req / 60 s |
| Token refresh | 20 req / 60 s |
| General auth | 3 req / 60 s |
| User service | 50 req / 60 s |
| Restaurant service | 5 req / 60 s |
| Menu service | 5 req / 60 s |
| Search service | 50 req / 60 s |
| Cart / Order / Payment | 40 req / 60 s |

The gateway validates the JWT, extracts the authenticated `userId`, and forwards it downstream as a `loggedInUser` request header so individual services do not need to re-parse the token.

---

### 3. Auth Service

| Property | Value |
|----------|-------|
| **Port** | `8081` |
| **Database** | MySQL — `auth_db` |
| **Role** | OTP-based authentication, JWT issuance, token refresh |

**Features**
- OTP delivery via SMS (Twilio) and Email (JavaMail + Thymeleaf templates)
- Short-lived **JWT access token** + long-lived **refresh token** (HttpOnly cookie, 30 days)
- Failed login attempt tracking and account lockout
- Kafka event publishing on user activity

**Kafka Events Published**

| Topic | Trigger |
|-------|---------|
| `user-verification` | New user signup — OTP verification |
| `login-attempt` | Every login attempt |

**Endpoints** — base path `/api/v1.0/auth`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/signup/send-otp` | Request signup OTP |
| `POST` | `/signup/verify-otp` | Verify OTP and create account |
| `POST` | `/signup/resend-otp` | Resend signup OTP |
| `POST` | `/login/send-otp` | Request login OTP |
| `POST` | `/login/verify-otp` | Verify OTP and receive tokens |
| `POST` | `/login/resend-otp` | Resend login OTP |
| `POST` | `/refresh` | Refresh access token using cookie |
| `PUT` | `/logout` | Invalidate tokens |

---

### 4. User Service

| Property | Value |
|----------|-------|
| **Port** | `8082` |
| **Database** | MySQL — `user_db` |
| **Role** | User profile and address management |

**Endpoints** — base path `/api/v1.0/user`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/info` | Get logged-in user profile |
| `POST` | `/address/add` | Add a new delivery address |
| `GET` | `/address/get` | List all saved addresses |
| `GET` | `/address/get/{addressId}` | Get a specific address |
| `PUT` | `/address/update/{addressId}` | Update address by ID |
| `PUT` | `/address/update/name/{name}` | Update address by label |
| `DELETE` | `/address/delete/{addressId}` | Delete an address |

All endpoints require the `loggedInUser` header (injected by the API Gateway after JWT validation).

---

### 5. Email Service

| Property | Value |
|----------|-------|
| **Port** | Internal (no HTTP exposure) |
| **Role** | Asynchronous email delivery |

A background service with no REST API. It consumes Kafka topics and sends transactional emails using HTML templates (Thymeleaf) with retry support via **Spring Retry**.

**Kafka Topics Consumed**

| Topic | Action |
|-------|--------|
| `user-verification` | Send OTP verification email |
| `login-attempt` | *(planned)* Send suspicious-login notification |

---

### 6. Restaurant Service

| Property | Value |
|----------|-------|
| **Port** | `8083` |
| **Database** | MySQL — `restaurant_db` |
| **Role** | Restaurant registration and management |

**Endpoints** — base path `/api/v1.0/restaurant`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/restaurant/create` | `ROLE_RESTAURANT_OWNER` | Register a new restaurant |
| `GET` | `/internal/restaurant-name` | Internal | Get restaurant name by ID |
| `GET` | `/internal/restaurant-id` | Internal | Get restaurant ID for an owner |

Internal endpoints are used for service-to-service calls and are not exposed via the Gateway.

---

### 7. Menu Service

| Property | Value |
|----------|-------|
| **Port** | `8084` |
| **Database** | MySQL — `menu_db` |
| **Role** | Menu and category management, image storage on AWS S3 |

Menu item images are uploaded to **AWS S3**. Create / update / delete operations publish Kafka events so the Search Service stays in sync.

**Kafka Events Published**

| Topic | Trigger |
|-------|---------|
| `menu-item-created` | New menu item added |
| `menu-item-updated` | Menu item details changed |
| `menu-item-deleted` | Menu item removed |

**Endpoints** — base path `/api/v1.0/menu`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/add-item` | `ROLE_RESTAURANT_OWNER` | Add item with image (multipart) |
| `POST` | `/add-category` | `ROLE_RESTAURANT_OWNER` | Add a menu category |
| `GET` | `/get-menu-items` | Any | List all items for a restaurant |
| `PUT` | `/update-menu-item/{menuItemId}` | `ROLE_RESTAURANT_OWNER` | Update item (with optional image) |
| `DELETE` | `/delete-menu-item/{menuItemId}` | `ROLE_RESTAURANT_OWNER` | Delete a menu item |
| `POST` | `/upload-all` | `ROLE_RESTAURANT_OWNER` | Bulk sync all items to search index |

---

### 8. Search Service

| Property | Value |
|----------|-------|
| **Port** | `8086` |
| **Database** | Elasticsearch |
| **Role** | Full-text search over menu items and restaurants |

Indexes are kept current by consuming Kafka events from the Menu Service. Supports rich filtering including text query, restaurant, tags, category, price range, and availability.

**Kafka Topics Consumed**

| Topic | Action |
|-------|--------|
| `menu-item-created` | Index new menu item document |
| `menu-item-updated` | Update existing document |
| `menu-item-deleted` | Remove document from index |

**Endpoints** — base path `/api/v1.0/search`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/item` | Search menu items (query, restaurant-id, tags, category, price range, availability) |
| `GET` | `/all-items` | Retrieve all indexed menu items |
| `GET` | `/all-restaurants` | Retrieve all indexed restaurants |
| `GET` | `/restaurant` | Get restaurant document by ID |

---

### 9. Cart Service

| Property | Value |
|----------|-------|
| **Port** | `8085` |
| **Database** | MongoDB |
| **Role** | Shopping cart management |

Uses MongoDB for flexible, schema-less cart documents. Communicates with the Menu Service via OpenFeign to resolve menu item details and prices.

**Endpoints** — base path `/api/v1.0/cart`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/add` | Add an item to cart |
| `PUT` | `/update` | Increase or decrease item quantity |
| `GET` | `/get-items` | Get all items in the user's cart |
| `GET` | `/get-items-quantity` | Get items and quantities grouped by restaurant |
| `GET` | `/get-items-count` | Get total number of items in cart |

**Request Bodies**

- `AddItemRequest` — `{ restaurantId, menuItemId }`
- `UpdateItemRequest` — `{ restaurantId, menuItemId, increase: boolean }`

---

### 10. Order Service

| Property | Value |
|----------|-------|
| **Port** | `8092` |
| **Database** | MySQL — `order_db` |
| **Role** | Order lifecycle management |

Converts a user's cart into a persisted order, orchestrates payment and delivery, and tracks the full order status timeline.

**Order Lifecycle**

```
PENDING → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED
                                        ↘ CANCELLED
```

**Kafka Topics**

| Direction | Topic | Purpose |
|-----------|-------|---------|
| Publishes | `order-restaurant-notification-topic` | New order alert to restaurant |
| Consumes | `order-restaurant-notification-topic` | Restaurant acceptance updates |
| Consumes | `order-payment-notification-topic` | Payment status updates |

**Endpoints** — base path `/api/v1.0/order`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/create` | `ROLE_CUSTOMER` | Create order from current cart |
| `PUT` | `/status` | `ROLE_RESTAURANT_OWNER` / `ROLE_DELIVERY_AGENT` | Update order status |

---

### 11. Payment Service

| Property | Value |
|----------|-------|
| **Port** | `8093` |
| **Database** | MySQL — `payment_db` |
| **Role** | Payment initiation and verification |

Integrates with **Razorpay** (primary) and **Stripe** (secondary). The service creates a payment order on the gateway, returns the gateway order ID to the frontend, and verifies the signature once the customer completes payment.

**Kafka Topics**

| Direction | Topic | Purpose |
|-----------|-------|---------|
| Publishes | `order-payment-notification-topic` | Payment success/failure notification |
| Consumes | `order-payment-notification-topic` | Internal payment event processing |

**Endpoints** — base path `/api/v1.0/payment`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/initiate` | Create payment order (`orderId`, `amount`, `customerId`) |
| `POST` | `/verify` | Verify payment and update status |
| `PUT` | `/failed` | Mark a payment as failed |

---

### 12. Delivery Service

| Property | Value |
|----------|-------|
| **Port** | `8091` |
| **Database** | None (stateless) |
| **Role** | Delivery assignment and tracking |

**Endpoints** — base path `/api/v1/delivery`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/{orderId}` | Assign delivery agent to an order |

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.3 |
| Service Discovery | Spring Cloud Netflix Eureka 2025.0.0 |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Messaging | Apache Kafka 3.9.1 |
| Cache / Rate Limiting | Redis 7 |
| Relational DB | MySQL 8 |
| Document DB | MongoDB |
| Search Engine | Elasticsearch 8 |
| Security | Spring Security + JJWT / Nimbus JWT |
| HTTP Clients | Spring Cloud OpenFeign, WebClient |
| Object Storage | AWS S3 |
| Payments | Razorpay, Stripe |
| SMS | Twilio |
| Email | JavaMail + Thymeleaf templates |
| Build | Maven (multi-module) |
| Code Generation | Lombok |

---

## Infrastructure

The `docker-compose.yml` at the project root starts the shared infrastructure dependencies:

```yaml
# Start all infrastructure
docker compose up -d
```

| Service | Image | Port | Notes |
|---------|-------|------|-------|
| Redis | `redis:latest` | `6379` | Rate limiting + JWT cache |
| Kafka | `apache/kafka:3.9.1` | `9092` | KRaft mode (no ZooKeeper), 3 partitions |

> **Not in docker-compose**: MySQL, MongoDB, and Elasticsearch must be provisioned separately (local install, managed cloud, or custom compose extension).

---

## Kafka Event Bus

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `user-verification` | Auth Service | Email Service | OTP verification emails on signup |
| `login-attempt` | Auth Service | Email Service *(planned)* | Login attempt audit and alerts |
| `menu-item-created` | Menu Service | Search Service | Index new menu item in Elasticsearch |
| `menu-item-updated` | Menu Service | Search Service | Update menu item document |
| `menu-item-deleted` | Menu Service | Search Service | Remove menu item from index |
| `order-restaurant-notification-topic` | Order Service | Restaurant Service | New order notification to restaurant |
| `order-payment-notification-topic` | Payment Service | Order Service | Payment result notification |

---

## Inter-Service Communication

### Synchronous (OpenFeign / WebClient)

| Caller | Called | Purpose |
|--------|--------|---------|
| Menu Service | Restaurant Service | Validate owner and get restaurant name |
| Cart Service | Menu Service | Resolve menu item details and prices |
| Order Service | Cart Service | Convert cart to order items |
| Order Service | Restaurant Service | Fetch restaurant details |
| Order Service | Payment Service | Initiate payment |
| Order Service | Delivery Service | Assign delivery |

### Asynchronous (Kafka)

| Publisher | Subscriber | Topic |
|-----------|-----------|-------|
| Auth Service | Email Service | `user-verification`, `login-attempt` |
| Menu Service | Search Service | `menu-item-*` |
| Order Service | Restaurant Service | `order-restaurant-notification-topic` |
| Payment Service | Order Service | `order-payment-notification-topic` |

---

## Security & Authentication

### Authentication Flow

```
1. User submits email/phone
2. Auth Service generates OTP → delivers via SMS (Twilio) + Email
3. User submits OTP
4. Auth Service validates OTP → issues:
     • Access Token  (JWT, short-lived) — returned in response body
     • Refresh Token (JWT, 30 days)    — set in HttpOnly cookie
5. All subsequent requests carry the Access Token in Authorization header
6. API Gateway validates JWT → injects `loggedInUser` header (userId)
7. On access token expiry → POST /auth/refresh (uses cookie automatically)
```

### Roles

| Role | Capabilities |
|------|-------------|
| `ROLE_CUSTOMER` | Browse, search, cart, order, payment |
| `ROLE_RESTAURANT_OWNER` | Create/manage restaurant, menu, accept/reject orders |
| `ROLE_DELIVERY_AGENT` | View and update delivery status |
| `ROLE_ADMIN` | Full system access |

---

## Database Schema

| Service | Database Name | Type | Key Entities |
|---------|--------------|------|-------------|
| Auth Service | `auth_db` | MySQL | User, OTP, RefreshToken, UserRole |
| User Service | `user_db` | MySQL | User, Address |
| Restaurant Service | `restaurant_db` | MySQL | Restaurant, RestaurantOwner |
| Menu Service | `menu_db` | MySQL | MenuItem, MenuCategory, MenuImage |
| Order Service | `order_db` | MySQL | Order, OrderItem, OrderStatus |
| Payment Service | `payment_db` | MySQL | Payment, PaymentTransaction |
| Cart Service | — | MongoDB | Cart (document-based) |
| Search Service | — | Elasticsearch | MenuItemDocument, RestaurantDocument |

---

## External Integrations

| Integration | Service(s) | Purpose |
|-------------|-----------|---------|
| **Twilio** | Auth Service | SMS OTP delivery |
| **JavaMail / SMTP** | Email Service | Transactional email delivery |
| **AWS S3** | Menu Service | Menu item image storage |
| **Razorpay** | Payment Service | Primary payment gateway |
| **Stripe** | Payment Service | Secondary payment gateway |
| **Elasticsearch** | Search Service | Full-text search index |

---

## Environment Variables

Create a `.env` file or set these as system/container environment variables before starting services.

```env
# ── JWT ──────────────────────────────────────────────────────────────────────
JWT_SECRET=<base64-secret>
JWT_ENC_SECRET=<base64-enc-secret>
JWT_EXP_TIME_MS=900000           # 15 minutes

JWT_REFRESH_SECRET=<base64-refresh-secret>
JWT_REF_ENC_SECRET=<base64-ref-enc-secret>
JWT_REF_EXP_TIME_MS=2592000000   # 30 days

# ── MySQL ─────────────────────────────────────────────────────────────────────
MYSQL_USERNAME=root
MYSQL_PASSWORD=<password>

# ── MongoDB ───────────────────────────────────────────────────────────────────
MONGODB_URI=mongodb://localhost:27017
MONGODB_DATABASE=cart_db

# ── Elasticsearch ─────────────────────────────────────────────────────────────
ELASTIC_HOST=https://localhost:9200
ELASTIC_PASSWORD=<password>
TRUSTSTORE_LOCATION=<path-to-truststore.jks>
TRUSTSTORE_PASSWORD=<password>

# ── AWS S3 ────────────────────────────────────────────────────────────────────
AWS_ACCESS_KEY=<access-key>
AWS_SECRET_KEY=<secret-key>
AWS_REGION=ap-south-1
AWS_S3_BUCKET=<bucket-name>

# ── Email / SMTP ──────────────────────────────────────────────────────────────
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=<gmail-address>
MAIL_PASSWORD=<app-password>

# ── Payments ──────────────────────────────────────────────────────────────────
RAZORPAY_API_KEY=<key>
RAZORPAY_API_SECRET=<secret>

# ── Twilio (SMS OTP) ──────────────────────────────────────────────────────────
TWILIO_ACCOUNT_SID=<sid>
TWILIO_AUTH_TOKEN=<token>
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- MySQL 8 (running locally or remote)
- MongoDB (running locally or remote)
- Elasticsearch 8 (running locally or remote)

### 1. Start Infrastructure

```bash
docker compose up -d
```

This starts **Redis** and **Kafka**.

### 2. Configure Environment

Copy the variables from [Environment Variables](#environment-variables) into your shell or a `.env` file, filling in real values.

### 3. Build All Modules

```bash
mvn clean package -DskipTests
```

### 4. Start Services (Recommended Order)

```bash
# 1. Service Registry — must be first
java -jar service-registry/target/*.jar

# 2. API Gateway
java -jar api-gateway/target/*.jar

# 3. Core services (can be started in parallel)
java -jar auth-service/target/*.jar
java -jar user-service/target/*.jar
java -jar email-service/target/*.jar
java -jar restaurant-service/target/*.jar
java -jar menu-service/target/*.jar
java -jar search-service/target/*.jar
java -jar cart-service/target/*.jar

# 4. Transactional services
java -jar order-service/target/*.jar
java -jar payment-service/target/*.jar
java -jar delivery-service/target/*.jar
```

### 5. Verify

Open the Eureka dashboard at [http://localhost:8761](http://localhost:8761) — all registered services should appear as `UP`.

---

## API Reference

All client requests go through the API Gateway on port `9000`.

```
Base URL: http://localhost:9000
```

### Authentication

```http
POST /auth/api/v1.0/auth/signup/send-otp
POST /auth/api/v1.0/auth/signup/verify-otp
POST /auth/api/v1.0/auth/login/send-otp
POST /auth/api/v1.0/auth/login/verify-otp
POST /auth/api/v1.0/auth/refresh
PUT  /auth/api/v1.0/auth/logout
```

### User

```http
GET    /user/api/v1.0/user/info
POST   /user/api/v1.0/user/address/add
GET    /user/api/v1.0/user/address/get
PUT    /user/api/v1.0/user/address/update/{id}
DELETE /user/api/v1.0/user/address/delete/{id}
```

### Restaurant & Menu

```http
POST   /restaurant/api/v1.0/restaurant/create
POST   /menu/api/v1.0/menu/add-item
GET    /menu/api/v1.0/menu/get-menu-items
PUT    /menu/api/v1.0/menu/update-menu-item/{id}
DELETE /menu/api/v1.0/menu/delete-menu-item/{id}
```

### Search

```http
GET /search/api/v1.0/search/item?query=burger&category=fast-food&minPrice=50&maxPrice=300
GET /search/api/v1.0/search/restaurant?restaurant-id={id}
```

### Cart

```http
POST /cart/api/v1.0/cart/add
PUT  /cart/api/v1.0/cart/update
GET  /cart/api/v1.0/cart/get-items
```

### Order

```http
POST /order/api/v1.0/order/create
PUT  /order/api/v1.0/order/status
```

### Payment

```http
POST /payment/api/v1.0/payment/initiate
POST /payment/api/v1.0/payment/verify
PUT  /payment/api/v1.0/payment/failed
```

---

## License

This project is licensed under the terms of the [LICENSE](LICENSE) file included in the repository.
