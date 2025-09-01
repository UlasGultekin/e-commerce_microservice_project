# 🏗️ Mikroservis Mimarisi - Detaylı Dokümantasyon

Bu dokümantasyon, Mikroservis projesinin detaylı mimari yapısını, her servisin kullandığı bağımlılıkları ve endpoint'lerini içerir.

## 📋 İçindekiler
- [Mimari Genel Bakış](#mimari-genel-bakış)
- [Servis Detayları](#servis-detayları)
- [Bağımlılık Yapısı](#bağımlılık-yapısı)
- [Endpoint Dokümantasyonu](#endpoint-dokümantasyonu)
- [İletişim Mimarisi](#iletişim-mimarisi)
- [Veritabanı Yapısı](#veritabanı-yapısı)

## 🏗️ Mimari Genel Bakış

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                CLIENT LAYER                                    │
│                         (Frontend/API Consumer)                                │
└─────────────────────┬───────────────────────────────────────────────────────────┘
                      │ HTTP/HTTPS
                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              API GATEWAY                                       │
│                              Port: 8080                                        │
│  • Spring Cloud Gateway                                                        │
│  • Load Balancing                                                              │
│  • CORS Configuration                                                          │
│  • Service Discovery Integration                                               │
└─────────────────────┬───────────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            EUREKA SERVER                                       │
│                              Port: 8761                                        │
│  • Service Discovery & Registry                                                │
│  • Health Monitoring                                                           │
│  • Service Instance Management                                                 │
└─────────────────────┬───────────────────────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│AUTH SERVICE │ │PRODUCT SVC  │ │ORDER SERVICE│
│   Port:8081 │ │  Port:8082  │ │  Port:8083  │
└─────────────┘ └─────────────┘ └──────┬──────┘
        │             │                │
        ▼             ▼                ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  auth_db    │ │ product_db  │ │  order_db   │
│ PostgreSQL  │ │ PostgreSQL  │ │ PostgreSQL  │
└─────────────┘ └─────────────┘ └─────────────┘
                                        │
                                        ▼ RabbitMQ
                               ┌─────────────────┐
                               │PAYMENT SERVICE  │
                               │   Port: 8084    │
                               │  (Stateless)    │
                               └─────────────────┘
```

## 🔧 Servis Detayları

### 🚪 API Gateway (Port: 8080)
**Sorumluluklar:**
- Tek giriş noktası (Single Entry Point)
- Load balancing ve routing
- CORS yapılandırması
- Servis keşfi ile dinamik routing

**Bağımlılıklar:**
- `org.springframework.cloud:spring-cloud-starter-gateway`
- `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
- `org.springframework.cloud:spring-cloud-starter-loadbalancer`
- `org.springframework.boot:spring-boot-starter-test`

**Endpoint'ler:**
- Tüm servis endpoint'lerine proxy görevi görür
- `/api/v1/auth/*` → Auth Service (8081)
- `/api/v1/products/*` → Product Service (8082)
- `/api/v1/orders/*` → Order Service (8083)

---

### 🔐 Auth Service (Port: 8081)
**Sorumluluklar:**
- Kullanıcı kaydı ve girişi
- JWT token üretimi ve doğrulama
- Refresh token yönetimi
- Kimlik doğrulama endpoint'leri

**Veritabanı:** `auth_db` (PostgreSQL)
**Tablolar:** `users`, `refresh_tokens`

**Bağımlılıklar:**
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-oauth2-resource-server`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
- `org.postgresql:postgresql`
- `io.jsonwebtoken:jjwt-api:0.12.5`
- `io.jsonwebtoken:jjwt-impl:0.12.5`
- `io.jsonwebtoken:jjwt-jackson:0.12.5`
- `org.springframework.boot:spring-boot-starter-actuator`
- `org.projectlombok:lombok`
- `org.springframework.boot:spring-boot-starter-test`

**Endpoint'ler:**
- `POST /api/v1/auth/register` - Kullanıcı kaydı
- `POST /api/v1/auth/login` - Kullanıcı girişi
- `GET /api/v1/auth/me` - Mevcut kullanıcı bilgileri
- `POST /api/v1/auth/refresh` - Token yenileme
- `POST /api/v1/auth/logout` - Çıkış yapma

---

### 🏢 Eureka Server (Port: 8761)
**Sorumluluklar:**
- Service discovery ve registry
- Mikroservisler arası haritası
- Health monitoring
- Service instance management

**Bağımlılıklar:**
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.cloud:spring-cloud-starter-netflix-eureka-server`
- `org.springframework.boot:spring-boot-starter-test`

**Endpoint'ler:**
- `GET /` - Eureka Dashboard
- `GET /eureka/apps` - Kayıtlı servisler
- `GET /eureka/apps/{service-name}` - Belirli servis bilgileri

---

### 📦 Product Service (Port: 8082)
**Sorumluluklar:**
- Ürün CRUD işlemleri
- Stok yönetimi
- JWT ile korumalı endpoint'ler
- Ürün sahipliği kontrolü

**Veritabanı:** `product_db` (PostgreSQL)
**Tablolar:** `products`

**Bağımlılıklar:**
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
- `org.postgresql:postgresql`
- `io.jsonwebtoken:jjwt-api:0.12.5`
- `io.jsonwebtoken:jjwt-impl:0.12.5`
- `io.jsonwebtoken:jjwt-jackson:0.12.5`
- `org.springframework.boot:spring-boot-starter-actuator`
- `org.projectlombok:lombok`
- `org.springframework.boot:spring-boot-starter-test`

**Endpoint'ler:**
- `GET /api/v1/products` - Sayfalı ürün listesi
- `GET /api/v1/products/{id}` - Belirli ürün detayı
- `POST /api/v1/products` - Yeni ürün oluşturma
- `PUT /api/v1/products/{id}` - Ürün güncelleme
- `DELETE /api/v1/products/{id}` - Ürün silme
- `POST /api/v1/products/{id}/reduce-stock` - Stok azaltma (Internal)

---

### 🛒 Order Service (Port: 8083)
**Sorumluluklar:**
- Çoklu ürün sipariş oluşturma
- Product Service ile HTTP iletişimi (Feign + Service Discovery)
- Payment Service ile RabbitMQ iletişimi
- Customer sahipliği kontrolü

**Veritabanı:** `order_db` (PostgreSQL)
**Tablolar:** `orders`, `order_items`

**Bağımlılıklar:**
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.postgresql:postgresql`
- `org.springframework.cloud:spring-cloud-starter-openfeign`
- `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
- `org.springframework.cloud:spring-cloud-starter-loadbalancer`
- `org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j`
- `org.springframework.boot:spring-boot-starter-amqp`
- `io.jsonwebtoken:jjwt-api:0.12.5`
- `io.jsonwebtoken:jjwt-impl:0.12.5`
- `io.jsonwebtoken:jjwt-jackson:0.12.5`
- `org.springframework.boot:spring-boot-starter-actuator`
- `org.projectlombok:lombok`
- `org.springframework.boot:spring-boot-starter-test`

**Endpoint'ler:**
- `POST /api/v1/orders` - Yeni sipariş oluşturma
- `GET /api/v1/orders/{id}` - Belirli sipariş detayı
- `GET /api/v1/orders` - Kullanıcının siparişleri

---

### 💳 Payment Service (Port: 8084)
**Sorumluluklar:**
- Mock ödeme işlemleri
- RabbitMQ ile Order Service iletişimi
- %90 başarı oranında sahte ödeme

**Veritabanı:** Yok (stateless)

**Bağımlılıklar:**
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-amqp`
- `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-actuator`
- `org.projectlombok:lombok`
- `org.springframework.boot:spring-boot-starter-test`

**Endpoint'ler:**
- RabbitMQ Listener: `payment.requests` queue
- RabbitMQ Publisher: `payment.results` queue

## 📡 Endpoint Dokümantasyonu

### 🔐 Auth Service Endpoints

#### `POST /api/v1/auth/register`
**Amaç:** Yeni kullanıcı kaydı oluşturur
**Güvenlik:** Public
**Request Body:**
```json
{
  "username": "string",
  "password": "string",
  "role": "string (optional)"
}
```
**Response:**
```json
{
  "status": "registered",
  "role": "ROLE_CUSTOMER|ROLE_SHOP_OWNER"
}
```

#### `POST /api/v1/auth/login`
**Amaç:** Kullanıcı girişi ve JWT token alımı
**Güvenlik:** Public
**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```
**Response:**
```json
{
  "access_token": "string",
  "refresh_token": "string",
  "token_type": "Bearer"
}
```

#### `GET /api/v1/auth/me`
**Amaç:** Mevcut kullanıcı bilgilerini getirir
**Güvenlik:** JWT Required
**Headers:** `Authorization: Bearer {token}`
**Response:**
```json
{
  "username": "string",
  "role": "string"
}
```

#### `POST /api/v1/auth/refresh`
**Amaç:** Access token yenileme
**Güvenlik:** Refresh Token Required
**Request Body:**
```json
{
  "refreshToken": "string"
}
```

#### `POST /api/v1/auth/logout`
**Amaç:** Kullanıcı çıkışı
**Güvenlik:** Refresh Token Required
**Request Body:**
```json
{
  "refreshToken": "string"
}
```

### 📦 Product Service Endpoints

#### `GET /api/v1/products`
**Amaç:** Sayfalı ürün listesi getirir
**Güvenlik:** JWT Required
**Query Parameters:** `page`, `size`
**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "string",
      "stock": 0,
      "price": 0.00,
      "ownerUsername": "string"
    }
  ],
  "pageable": {...},
  "totalElements": 0
}
```

#### `GET /api/v1/products/{id}`
**Amaç:** Belirli bir ürünü getirir
**Güvenlik:** JWT Required
**Response:**
```json
{
  "id": 1,
  "name": "string",
  "stock": 0,
  "price": 0.00,
  "ownerUsername": "string"
}
```

#### `POST /api/v1/products`
**Amaç:** Yeni ürün oluşturur
**Güvenlik:** JWT Required (ROLE_SHOP_OWNER)
**Request Body:**
```json
{
  "name": "string",
  "stock": 0,
  "price": 0.00
}
```

#### `PUT /api/v1/products/{id}`
**Amaç:** Mevcut ürünü günceller
**Güvenlik:** JWT Required (Owner only)
**Request Body:**
```json
{
  "name": "string",
  "stock": 0,
  "price": 0.00
}
```

#### `DELETE /api/v1/products/{id}`
**Amaç:** Ürünü siler
**Güvenlik:** JWT Required (Owner only)

#### `POST /api/v1/products/{id}/reduce-stock`
**Amaç:** Stok azaltma (Internal use)
**Güvenlik:** JWT Required
**Request Body:**
```json
{
  "quantity": 0
}
```

### 🛒 Order Service Endpoints

#### `POST /api/v1/orders`
**Amaç:** Yeni sipariş oluşturur (çoklu ürün destekli)
**Güvenlik:** JWT Required (ROLE_CUSTOMER)
**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```
**Response:**
```json
{
  "id": 1,
  "customerUsername": "string",
  "items": [
    {
      "productId": 1,
      "productName": "string",
      "quantity": 2,
      "unitPrice": 0.00,
      "totalPrice": 0.00
    }
  ],
  "totalAmount": 0.00,
  "status": "CREATED"
}
```

#### `GET /api/v1/orders/{id}`
**Amaç:** Belirli siparişi getirir (sadece sahibi)
**Güvenlik:** JWT Required

#### `GET /api/v1/orders`
**Amaç:** Kullanıcının tüm siparişlerini listeler
**Güvenlik:** JWT Required

## 🔄 İletişim Mimarisi

### HTTP İletişimi (Synchronous)
```
Order Service --[OpenFeign HTTP]--> Product Service
    │                               │
    ├── GET /api/v1/products/{id}      │
    ├── POST /api/v1/products/{id}/reduce-stock │
    ├── Authorization: Bearer ...    │
    └── Response: ProductInfo       │
```

### RabbitMQ İletişimi (Asynchronous)
```
Exchange: payments.exchange
├── Queue: payment.requests
│   ├── Producer: Order Service
│   └── Consumer: Payment Service
└── Queue: payment.results
    ├── Producer: Payment Service
    └── Consumer: Order Service
```

**Mesaj Formatları:**
```json
// Payment Request
{
  "orderId": 1,
  "amount": 999.99
}

// Payment Result  
{
  "orderId": 1,
  "status": "PAID|FAILED"
}
```

## 🗄️ Veritabanı Yapısı

### auth_db.users
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

### auth_db.refresh_tokens
```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### product_db.products
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stock INTEGER NOT NULL CHECK (stock >= 0),
    price DECIMAL(12,2) NOT NULL,
    owner_username VARCHAR(255) NOT NULL
);
```

### order_db.orders
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_username VARCHAR(255) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### order_db.order_items
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL
);
```

## 🔒 Güvenlik Mimarisi

### JWT Token Yapısı
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
{
  "sub": "username",
  "role": "ROLE_CUSTOMER|ROLE_SHOP_OWNER",
  "iss": "mikro-auth",
  "iat": 1640995200,
  "exp": 1640998800
}
```

### Güvenlik Kontrolleri
- **Auth Service**: Sadece `/register` ve `/login` public
- **Product Service**: Tüm endpoint'ler JWT gerektirir
- **Order Service**: Tüm endpoint'ler JWT gerektirir
- **Payment Service**: Internal (sadece RabbitMQ)

### Role-Based Access Control
- **ROLE_CUSTOMER**: Sipariş oluşturabilir, ürün görüntüleyebilir
- **ROLE_SHOP_OWNER**: Ürün CRUD işlemleri yapabilir, sipariş görüntüleyebilir

## 📊 Bağımlılık Özeti

| Servis | Web | Security | JPA | Eureka | Feign | AMQP | JWT | Actuator | Lombok |
|--------|-----|----------|-----|--------|-------|------|-----|----------|--------|
| API Gateway | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Auth Service | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ |
| Eureka Server | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Order Service | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Payment Service | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ | ✅ |
| Product Service | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ |

## 🎯 Özel Kütüphaneler ve Amaçları

- **JJWT (0.12.5)**: JWT token oluşturma, doğrulama ve parsing
- **OpenFeign**: Declarative HTTP client (Order → Product Service)
- **Resilience4j**: Circuit breaker pattern (Order Service)
- **Spring Cloud Gateway**: API Gateway ve routing
- **Spring Cloud LoadBalancer**: Client-side load balancing
- **Spring AMQP**: RabbitMQ messaging (Order ↔ Payment Service)
- **Spring Data JPA**: Database operations ve ORM
- **Spring Security**: Authentication ve authorization
- **Lombok**: Boilerplate code reduction

---

**Bu dokümantasyon, Mikroservis projesinin tüm teknik detaylarını içerir ve geliştiriciler için kapsamlı bir referans kaynağıdır.**
