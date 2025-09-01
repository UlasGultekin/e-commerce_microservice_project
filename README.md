# Mikroservis Demo - Spring Boot 3.3.x & Java 21

Bu repo 6 bağımsız servisten oluşan kapsamlı bir mikroservis demostrasyon projesidir. Eureka Server ile service discovery, API Gateway ile tek giriş noktası, Auth/Product/Order/Payment servisleri arasında hem HTTP (OpenFeign) hem de RabbitMQ ile asenkron iletişim kurulmuştur.

## 📋 İçindekiler
- [Mimari Genel Bakış](#mimari-genel-bakış)
- [Teknoloji Stack](#teknoloji-stack)
- [Kurulum ve Çalıştırma](#kurulum-ve-çalıştırma)
- [Servis Detayları](#servis-detayları)
- [API Dokümantasyonu](#api-dokümantasyonu)
- [Örnek Kullanım Senaryoları](#örnek-kullanım-senaryoları)
- [Güvenlik](#güvenlik)
- [İletişim Mimarisi](#iletişim-mimarisi)
- [Sorun Giderme](#sorun-giderme)

## 🏗️ Mimari Genel Bakış

```
                    ┌─────────────────┐
                    │   Client App    │
                    │  (Frontend/API) │
                    └─────────┬───────┘
                              │ HTTP
                              ▼
                    ┌─────────────────┐
                    │  API Gateway    │
                    │     (8080)      │ Load Balancer
                    └─────────┬───────┘
                              │
                    ┌─────────▼───────┐
                    │ Eureka Server   │
                    │     (8761)      │ Service Discovery
                    └─────────────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
    ┌─────────▼─────┐ ┌──────▼──────┐ ┌──────▼──────┐
    │ Auth Service  │ │Product Svc  │ │ Order Svc   │
    │    (8081)     │ │   (8082)    │ │   (8083)    │
    └───────────────┘ └─────────────┘ └──────┬──────┘
           │                │               │
           ▼                ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │  auth_db    │ │ product_db  │ │  order_db   │
    └─────────────┘ └─────────────┘ └─────────────┘
                                           │
                                           ▼ RabbitMQ
                                  ┌─────────────────┐
                                  │ Payment Service │
                                  │     (8084)      │
                                  └─────────────────┘
```

## 🛠️ Teknoloji Stack

### Backend
- **Spring Boot 3.3.2** - Ana framework
- **Java 21** - LTS version
- **Spring Cloud 2023.0.3** - Mikroservis toolkit
- **Spring Cloud Gateway** - API Gateway ve load balancing
- **Eureka Server/Client** - Service discovery
- **Spring Security** - JWT tabanlı güvenlik
- **Spring Data JPA** - ORM ve veritabanı erişimi
- **OpenFeign** - HTTP client (servisler arası)
- **RabbitMQ** - Asenkron mesajlaşma
- **PostgreSQL** - İlişkisel veritabanı

### DevOps & Tools
- **Docker Compose** - Altyapı konteynerizasyonu
- **Maven** - Bağımlılık yönetimi
- **JJWT** - JWT token işlemleri
- **Lombok** - Kod azaltma
- **Properties** - Konfigürasyon formatı (YAML yerine)

## 🚀 Kurulum ve Çalıştırma

### Ön Gereksinimler
- Java 21 JDK
- Maven 3.9+
- Docker & Docker Compose
- PowerShell (Windows)

### 1. Altyapıyı Başlatın
```powershell
cd C:\Users\Ulas\Desktop\mikro
docker compose up -d
```

**Kontrol Edilecek Servisler:**
- PostgreSQL: `localhost:5432` (postgres/postgres)
- RabbitMQ UI: `http://localhost:15672` (guest/guest)

### 2. Ortam Değişkenlerini Ayarlayın
```powershell
# JWT için güçlü bir secret ayarlayın
setx JWT_SECRET "2024-mikro-demo-very-strong-secret-key-change-in-production"

# Terminal'i yeniden başlatın veya:
$env:JWT_SECRET = "2024-mikro-demo-very-strong-secret-key-change-in-production"
```

### 3. Servisleri Başlatın (Sırasıyla)

**Terminal 1 - Eureka Server:**
```powershell
cd C:\Users\Ulas\Desktop\mikro\eureka-server
mvn clean compile
mvn spring-boot:run
```

**Terminal 2 - API Gateway:**
```powershell
cd C:\Users\Ulas\Desktop\mikro\api-gateway
mvn clean compile
mvn spring-boot:run
```

**Terminal 3 - Auth Service:**
```powershell
cd C:\Users\Ulas\Desktop\mikro\auth-service
mvn clean compile
mvn spring-boot:run
```

**Terminal 4 - Product Service:**
```powershell
cd C:\Users\Ulas\Desktop\mikro\product-service
mvn clean compile
mvn spring-boot:run
```

**Terminal 5 - Order Service:**
```powershell
cd C:\Users\Ulas\Desktop\mikro\order-service
mvn clean compile
mvn spring-boot:run
```

**Terminal 6 - Payment Service:**
```powershell
cd C:\Users\Ulas\Desktop\mikro\payment-service
mvn clean compile
mvn spring-boot:run
```

### 4. Durum Kontrolü
Tüm servisler başladıktan sonra:
```powershell
# Eureka Server (Service Registry)
curl http://localhost:8761

# API Gateway
curl http://localhost:8080/actuator/health

# Tüm servislere Gateway üzerinden erişim (önerilen)
curl http://localhost:8080/api/v1/auth/
curl http://localhost:8080/api/v1/products/
curl http://localhost:8080/api/v1/orders/

# Alternatif: Direkt servis erişimi
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # Product  
curl http://localhost:8083/actuator/health  # Order
curl http://localhost:8084/actuator/health  # Payment
```

## 🔧 Servis Detayları

### Eureka Server (Port: 8761)
**Sorumluluklar:**
- Service discovery ve registry
- Mikroservisler arası haritası
- Health monitoring

**UI:** `http://localhost:8761`

### API Gateway (Port: 8080)
**Sorumluluklar:**
- Tek giriş noktası (Single Entry Point)
- Load balancing ve routing
- CORS yapılandırması
- Servis keşfi ile dinamik routing

**Gateway URL:** `http://localhost:8080`

### Auth Service (Port: 8081)
**Sorumluluklar:**
- Kullanıcı kaydı ve girişi
- JWT token üretimi ve doğrulama
- Kimlik doğrulama endpoint'leri

**Veritabanı:** `auth_db`
**Tablolar:** `users`

### Product Service (Port: 8082)
**Sorumluluklar:**
- Ürün CRUD işlemleri
- Stok yönetimi
- JWT ile korumalı endpoint'ler
- Ürün sahipliği kontrolü

**Veritabanı:** `product_db`
**Tablolar:** `products`

### Order Service (Port: 8083)
**Sorumluluklar:**
- Çoklu ürün sipariş oluşturma
- Product Service ile HTTP iletişimi (Feign + Service Discovery)
- Payment Service ile RabbitMQ iletişimi
- Customer sahipliği kontrolü

**Veritabanı:** `order_db`
**Tablolar:** `orders`, `order_items`

### Payment Service (Port: 8084)
**Sorumluluklar:**
- Mock ödeme işlemleri
- RabbitMQ ile Order Service iletişimi
- %90 başarı oranında sahte ödeme

**Veritabanı:** Yok (stateless)

## 📡 API Dokümantasyonu

### 👥 Roller ve Yetkiler (Güncel)

- Roller:
  - `ROLE_CUSTOMER`: Son kullanıcı (alışveriş yapan)
  - `ROLE_SHOP_OWNER`: Dükkan sahibi (ürün yönetimi)

- Yetkiler:
  - Product: `GET` → her ikisi; `POST/PUT/DELETE` → sadece `ROLE_SHOP_OWNER`
  - Order: `POST` → sadece `ROLE_CUSTOMER`; `GET` → her ikisi
  - Product sahiplik: `PUT/DELETE` işlemleri sadece ürünü oluşturan shop owner (ownerUsername) tarafından yapılabilir

- Kayıt Sırasında Role Seçimi:
  - Request body'de `role` alanı aşağıdaki değerlerle eşleşir:
    - `customer`, `musteri`, `müşteri` → `ROLE_CUSTOMER`
    - `shop`, `shop_owner`, `store`, `dukkan`, `dükkan`, `satici`, `satıcı` → `ROLE_SHOP_OWNER`
  - Belirtilmezse veya eşleşmezse: `ROLE_CUSTOMER`

### 🔐 Auth Service - `/api/v1/auth`

#### POST `/api/v1/auth/register`
Yeni kullanıcı kaydı oluşturur. Role sistemi akıllı algoritma ile belirlenir.

**Role Belirleme Stratejisi (Güncel):**
1. `role` alanı `customer/musteri/müşteri` → `ROLE_CUSTOMER`
2. `role` alanı `shop/dukkan/dükkan/satici/satıcı/...` → `ROLE_SHOP_OWNER`
3. Aksi halde → `ROLE_CUSTOMER`

**Geçerli Role'ler:** `ROLE_CUSTOMER`, `ROLE_SHOP_OWNER`

**Request (Basit - Gateway üzerinden):**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "secure123"
  }'
```

**Request (Role Belirtmeli - Customer):**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer_ali",
    "password": "secure123",
    "role": "customer"
  }'
```

**Response (200):**
```json
{
  "status": "registered",
  "role": "ROLE_CUSTOMER"
}
```

**Request (Role Belirtmeli - Shop Owner):**
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "dukkan_veli",
    "password": "secure123",
    "role": "dukkan"
  }'
```

**Response (200):**
```json
{
  "status": "registered",
  "role": "ROLE_SHOP_OWNER"
}
```

**Response (400) - Kullanıcı zaten var:**
```json
{
  "error": "username_taken"
}
```

#### POST `/api/v1/auth/login`
Kullanıcı girişi ve JWT token alımı.

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "secure123"
  }'
```

**Response (200):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer"
}
```

**Response (401) - Hatalı giriş:**
```json
{
  "error": "invalid_credentials"
}
```

#### GET `/api/v1/auth/me`
Mevcut kullanıcı bilgilerini getirir.

**Request:**
```bash
curl -X GET http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200):**
```json
{
  "username": "john_doe",
  "role": "ROLE_USER"
}
```

### 📦 Product Service - `/api/v1/products`

> **Not:** Tüm Product endpoint'leri JWT token gerektirir.

#### GET `/api/v1/products`
Sayfalı ürün listesi getirir.

**Request:**
```bash
curl -X GET "http://localhost:8082/api/v1/products?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop",
      "stock": 50,
      "price": 2499.99
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1
}
```

#### GET `/api/v1/products/{id}`
Belirli bir ürünü getirir.

**Request:**
```bash
curl -X GET http://localhost:8082/api/v1/products/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200):**
```json
{
  "id": 1,
  "name": "Laptop",
  "stock": 50,
  "price": 2499.99
}
```

#### POST `/api/v1/products`
Yeni ürün oluşturur.

**Request:**
```bash
curl -X POST http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "stock": 100,
    "price": 29.99
  }'
```

**Response (200):**
```json
{
  "id": 2,
  "name": "Wireless Mouse",
  "stock": 100,
  "price": 29.99
}
```

#### PUT `/api/v1/products/{id}`
Mevcut ürünü günceller.

**Request:**
```bash
curl -X PUT http://localhost:8082/api/v1/products/2 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Gaming Mouse",
    "stock": 75,
    "price": 49.99
  }'
```

#### DELETE `/api/v1/products/{id}`
Ürünü siler.

**Request:**
```bash
curl -X DELETE http://localhost:8082/api/v1/products/2 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 🛒 Order Service - `/api/v1/orders`

> **Not:** Tüm Order endpoint'leri JWT token gerektirir. Sadece ROLE_CUSTOMER sipariş oluşturabilir.

#### POST `/api/v1/orders`
Yeni sipariş oluşturur (çoklu ürün destekli sepet sistemi).

**İşlem Akışı:**
1. Her ürün için Product Service'den bilgi ve stok kontrolü (HTTP/Feign)
2. Order ve OrderItem kayıtları oluşturulur (status: CREATED)
3. Toplam tutar hesaplanır
4. Payment Service'e ödeme isteği gönderilir (RabbitMQ)
5. Payment sonucu beklenir (async)

**Request (Tek Ürün):**
```bash
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 2}
    ]
  }'
```

**Request (Çoklu Ürün - Sepet):**
```bash
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 3, "quantity": 1},
      {"productId": 5, "quantity": 3}
    ]
  }'
```

**Response (200):**
```json
{
  "id": 1,
  "customerUsername": "customer_ali",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 2,
      "unitPrice": 999.99,
      "totalPrice": 1999.98
    },
    {
      "productId": 3,
      "productName": "Phone Case",
      "quantity": 1,
      "unitPrice": 29.99,
      "totalPrice": 29.99
    }
  ],
  "totalAmount": 2029.97,
  "status": "CREATED"
}
```

**Response (400) - Yetersiz stok:**
```json
{
  "error": "insufficient_stock_or_not_found",
  "productId": 3
}
```

#### GET `/api/v1/orders/{id}`
Belirli siparişi getirir (sadece sahibi görebilir).

**Request:**
```bash
curl -X GET http://localhost:8083/api/v1/orders/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200):**
```json
{
  "id": 1,
  "customerUsername": "customer_ali",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 2,
      "unitPrice": 999.99,
      "totalPrice": 1999.98
    }
  ],
  "totalAmount": 1999.98,
  "status": "PAID"
}
```

**Response (404) - Sipariş bulunamadı veya başka kullanıcının siparişi**

#### GET `/api/v1/orders`
Kullanıcının tüm siparişlerini listeler.

**Request:**
```bash
curl -X GET http://localhost:8083/api/v1/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200):**
```json
[
  {
    "id": 1,
    "customerUsername": "customer_ali",
    "items": [...],
    "totalAmount": 1999.98,
    "status": "PAID"
  },
  {
    "id": 2,
    "customerUsername": "customer_ali",
    "items": [...],
    "totalAmount": 599.99,
    "status": "CREATED"
  }
]
```

## 🎯 Örnek Kullanım Senaryoları

### Senaryo 1: E-ticaret Shop Owner ve Customer Akışı (API Gateway)

```bash
# Tüm istekler API Gateway (localhost:8080) üzerinden yapılır

# 1. Shop Owner kaydı
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "shop_alice", "password": "alice123", "role": "shop"}'
# Response: {"status": "registered", "role": "ROLE_SHOP_OWNER"}

# 2. Shop Owner token al
SHOP_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "shop_alice", "password": "alice123"}' | \
  jq -r '.access_token')

# 3. Shop Owner ürünleri ekler
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $SHOP_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "iPhone 15", "stock": 25, "price": 999.99}'

curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $SHOP_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Phone Case", "stock": 100, "price": 29.99}'

# 4. Customer kaydı
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "customer_bob", "password": "bob123", "role": "customer"}'
# Response: {"status": "registered", "role": "ROLE_CUSTOMER"}

# 5. Customer token al
CUSTOMER_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer_bob", "password": "bob123"}' | \
  jq -r '.access_token')

# 6. Customer ürünleri görüntüle
curl -X GET http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"

# 7. Customer sepet siparişi oluştur (çoklu ürün)
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 2, "quantity": 2}
    ]
  }'

# 8. Customer siparişlerini kontrol et
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"

# 9. Belirli sipariş detayı (bir kaç saniye bekleyip payment sonrası)
curl -X GET http://localhost:8080/api/v1/orders/1 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

### Senaryo 2: Stok Kontrolü ve Hata Yönetimi

```bash
# Customer token'ı önceki senaryodan kullanın

# 1. Mevcut stoktan fazla sipariş vermeye çalış
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 50}
    ]
  }'
# Response: {"error": "insufficient_stock_or_not_found", "productId": 1}

# 2. Olmayan ürün için sipariş vermeye çalış
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 999, "quantity": 1}
    ]
  }'
# Response: {"error": "insufficient_stock_or_not_found", "productId": 999}

# 3. Karışık sepet - bazı ürünler geçerli, bazıları değil
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 999, "quantity": 1}
    ]
  }'
# Response: {"error": "insufficient_stock_or_not_found", "productId": 999}
```

### Senaryo 3: Role Tabanlı Kullanıcı Yönetimi

```bash
# 1. Admin kullanıcı oluştur (username stratejisi)
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin_john", "password": "admin123"}'
# Response: {"status": "registered", "role": "ROLE_ADMIN"}

# 2. Manager kullanıcı oluştur (role request)
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "manager_sara", "password": "manager123", "role": "ROLE_MANAGER"}'
# Response: {"status": "registered", "role": "ROLE_MANAGER"}

# 3. Normal kullanıcı oluştur
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "customer_mike", "password": "customer123"}'
# Response: {"status": "registered", "role": "ROLE_USER"}

# 4. Geçersiz role ile deneme
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "hacker", "password": "hack123", "role": "ROLE_SUPERUSER"}'
# Response: {"status": "registered", "role": "ROLE_USER"} (varsayılan role)
```

### Senaryo 4: Ürün Yönetimi

```bash
# 1. Toplu ürün ekleme
products=('{"name": "MacBook Pro", "stock": 10, "price": 2999.99}'
          '{"name": "iPad Air", "stock": 30, "price": 599.99}'
          '{"name": "AirPods Pro", "stock": 100, "price": 249.99}')

for product in "${products[@]}"; do
  curl -X POST http://localhost:8082/api/v1/products \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$product"
done

# 2. Ürün güncelleme (stok artırma)
curl -X PUT http://localhost:8082/api/v1/products/2 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "iPad Air (Updated)", "stock": 50, "price": 549.99}'

# 3. Ürün listesini kontrol et
curl -X GET http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer $TOKEN"

# 4. Sahip olmayan kullanıcıyla güncelleme denemesi (403 beklenir)
OTHER=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer_mike", "password": "customer123"}' | jq -r '.access_token')

curl -X PUT http://localhost:8082/api/v1/products/2 \
  -H "Authorization: Bearer $OTHER" \
  -H "Content-Type: application/json" \
  -d '{"name": "Hack", "stock": 1, "price": 1.00}'
# Response: {"error":"forbidden_not_owner"}
```

## 🔒 Güvenlik

### JWT Token Yapısı
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
{
  "sub": "username",
  "role": "ROLE_USER",
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

### Token Geçiş Mekanizması
```
Client → Order Service (JWT)
     ↓
Order Service → Product Service (JWT forwarded via Feign)
```

## 🔄 İletişim Mimarisi

### HTTP İletişimi (Synchronous)
```
Order Service --[Feign HTTP]--> Product Service
    │                               │
    ├── GET /api/v1/products/{id}      │
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
  "status": "PAID" // or "FAILED"
}
```

### Veritabanı Yapısı

**auth_db.users:**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

**product_db.products:**
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stock INTEGER NOT NULL CHECK (stock >= 0),
    price DECIMAL(12,2) NOT NULL,
    owner_username VARCHAR(255) NOT NULL
);
```

**order_db.orders:**
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_username VARCHAR(255) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL
);

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

## 🐛 Sorun Giderme

### Yaygın Sorunlar ve Çözümleri

#### 1. JWT Secret Hatası
**Hata:** `JWT secret not configured`
**Çözüm:**
```powershell
setx JWT_SECRET "your-very-strong-secret-key"
# Terminal'i yeniden başlatın
```

#### 2. PostgreSQL Bağlantı Hatası
**Hata:** `Connection refused to localhost:5432`
**Çözüm:**
```powershell
# Docker Compose durumunu kontrol edin
docker compose ps

# PostgreSQL loglarını kontrol edin
docker compose logs postgres

# Yeniden başlatın
docker compose down
docker compose up -d
```

#### 3. RabbitMQ Bağlantı Hatası
**Hata:** `Connection refused to localhost:5672`
**Çözüm:**
```powershell
# RabbitMQ durumunu kontrol edin
docker compose logs rabbitmq

# Management UI'da kuyruları kontrol edin
# http://localhost:15672
```

#### 4. Feign Client Hatası
**Hata:** `FeignException: 401 Unauthorized`
**Çözüm:**
- JWT token'ın geçerli olduğundan emin olun
- `FeignConfig` sınıfında Authorization header forward'ının doğru olduğunu kontrol edin

#### 5. Maven Build Hatası
**Hata:** `Failed to execute goal compile`
**Çözüm:**
```powershell
# Dependency'leri temizle ve yeniden indir
mvn clean install -U

# Java version kontrol et
java -version  # 21 olmalı
```

### Debug ve Monitoring

#### Loglama Seviyeleri
Application.yml dosyalarında loglama seviyelerini artırabilirsiniz:
```yaml
logging:
  level:
    com.mikro: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
    org.springframework.amqp: DEBUG
```

#### RabbitMQ Monitoring
- Management UI: `http://localhost:15672`
- Queue durumları ve mesaj sayıları
- Consumer ve Producer aktiviteleri

#### Database Monitoring
```sql
-- PostgreSQL bağlantıları
SELECT * FROM pg_stat_activity;

-- Tablo boyutları
SELECT 
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation 
FROM pg_stats;
```

### Performance Tips

1. **Connection Pooling**: Varsayılan HikariCP ayarları production için optimize edilebilir
2. **JWT Cache**: Sık kullanılan token'ları cache'leyebilirsiniz
3. **Database Indexing**: Sık sorgulanan kolonlara index ekleyin
4. **RabbitMQ Optimization**: Message durability ve acknowledge ayarları

## 🏃‍♂️ Hızlı Test Scripti

Tüm sistemi test etmek için:

```powershell
# PowerShell test scripti
$baseAuth = "http://localhost:8081/api/v1/auth"
$baseProduct = "http://localhost:8082/api/v1/products"  
$baseOrder = "http://localhost:8083/api/v1/orders"

# 1. Register
$registerResult = Invoke-RestMethod -Uri "$baseAuth/register" -Method Post -ContentType "application/json" -Body '{"username":"testuser","password":"test123"}'
Write-Host "Register: $($registerResult.status)"

# 2. Login
$loginResult = Invoke-RestMethod -Uri "$baseAuth/login" -Method Post -ContentType "application/json" -Body '{"username":"testuser","password":"test123"}'
$token = $loginResult.access_token
Write-Host "Token alındı: $($token.Substring(0,20))..."

# 3. Create Product
$headers = @{ Authorization = "Bearer $token" }
$productResult = Invoke-RestMethod -Uri $baseProduct -Method Post -Headers $headers -ContentType "application/json" -Body '{"name":"Test Product","stock":10,"price":99.99}'
Write-Host "Product oluşturuldu: ID $($productResult.id)"

# 4. Create Order
$orderResult = Invoke-RestMethod -Uri $baseOrder -Method Post -Headers $headers -ContentType "application/json" -Body "{`"productId`":$($productResult.id),`"quantity`":2}"
Write-Host "Order oluşturuldu: ID $($orderResult.id), Status: $($orderResult.status)"

# 5. Check Order Status (after payment)
Start-Sleep -Seconds 3
$orderCheck = Invoke-RestMethod -Uri "$baseOrder/$($orderResult.id)" -Method Get -Headers $headers
Write-Host "Final Order Status: $($orderCheck.status)"
```

## 📞 Destek ve Katkı

Bu demo projesi için:
- Issues: GitHub Issues
- Questions: Discussions
- Contributions: Pull Requests welcome

## 📦 Servis Bağımlılıkları ve Kütüphaneler

Her serviste kullanılan bağımlılıklar ve kütüphaneler aşağıda detaylı olarak listelenmiştir:

### 🔧 API Gateway (Port: 8080)
**Ana Bağımlılıklar:**
- `spring-cloud-starter-gateway` - API Gateway ve routing
- `spring-cloud-starter-netflix-eureka-client` - Service discovery client
- `spring-cloud-starter-loadbalancer` - Load balancing
- `spring-boot-starter-test` - Test framework

**Versiyonlar:**
- Spring Boot: 3.3.2
- Spring Cloud: 2023.0.3
- Java: 21

### 🔐 Auth Service (Port: 8081)
**Ana Bağımlılıklar:**
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-validation` - Input validation
- `spring-boot-starter-security` - Security framework
- `spring-boot-starter-oauth2-resource-server` - OAuth2 resource server
- `spring-boot-starter-data-jpa` - JPA ve Hibernate
- `spring-cloud-starter-netflix-eureka-client` - Service discovery
- `postgresql` - PostgreSQL driver
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.5) - JWT token işlemleri
- `spring-boot-starter-actuator` - Health check ve monitoring
- `lombok` - Code generation
- `spring-boot-starter-test` - Test framework

**Versiyonlar:**
- Spring Boot: 3.3.2
- Spring Cloud: 2023.0.3
- JJWT: 0.12.5
- Java: 21

### 🏢 Eureka Server (Port: 8761)
**Ana Bağımlılıklar:**
- `spring-boot-starter-web` - Web server
- `spring-cloud-starter-netflix-eureka-server` - Service discovery server
- `spring-boot-starter-test` - Test framework

**Versiyonlar:**
- Spring Boot: 3.3.2
- Spring Cloud: 2023.0.3
- Java: 21

### 🛒 Order Service (Port: 8083)
**Ana Bağımlılıklar:**
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-validation` - Input validation
- `spring-boot-starter-security` - Security framework
- `spring-boot-starter-data-jpa` - JPA ve Hibernate
- `postgresql` - PostgreSQL driver
- `spring-cloud-starter-openfeign` - HTTP client (servisler arası iletişim)
- `spring-cloud-starter-netflix-eureka-client` - Service discovery
- `spring-cloud-starter-loadbalancer` - Load balancing
- `spring-cloud-starter-circuitbreaker-resilience4j` - Circuit breaker pattern
- `spring-boot-starter-amqp` - RabbitMQ client
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.5) - JWT token işlemleri
- `spring-boot-starter-actuator` - Health check ve monitoring
- `lombok` - Code generation
- `spring-boot-starter-test` - Test framework

**Versiyonlar:**
- Spring Boot: 3.3.2
- Spring Cloud: 2023.0.3
- JJWT: 0.12.5
- Java: 21

### 💳 Payment Service (Port: 8084)
**Ana Bağımlılıklar:**
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-amqp` - RabbitMQ client
- `spring-cloud-starter-netflix-eureka-client` - Service discovery
- `spring-boot-starter-validation` - Input validation
- `spring-boot-starter-actuator` - Health check ve monitoring
- `lombok` - Code generation
- `spring-boot-starter-test` - Test framework

**Versiyonlar:**
- Spring Boot: 3.3.2
- Spring Cloud: 2023.0.3
- Java: 21

### 📦 Product Service (Port: 8082)
**Ana Bağımlılıklar:**
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-validation` - Input validation
- `spring-boot-starter-security` - Security framework
- `spring-boot-starter-data-jpa` - JPA ve Hibernate
- `spring-cloud-starter-netflix-eureka-client` - Service discovery
- `postgresql` - PostgreSQL driver
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.5) - JWT token işlemleri
- `spring-boot-starter-actuator` - Health check ve monitoring
- `lombok` - Code generation
- `spring-boot-starter-test` - Test framework

**Versiyonlar:**
- Spring Boot: 3.3.2
- Spring Cloud: 2023.0.3
- JJWT: 0.12.5
- Java: 21

### 🗄️ Veritabanı ve Altyapı
**PostgreSQL:**
- Driver: `org.postgresql:postgresql`
- Versiyon: Spring Boot managed (latest compatible)

**RabbitMQ:**
- Client: `spring-boot-starter-amqp`
- Versiyon: Spring Boot managed (latest compatible)

### 🔧 Build ve Development Tools
**Maven Plugins:**
- `spring-boot-maven-plugin` - Spring Boot application packaging
- `maven-compiler-plugin` - Java compilation (Java 21)

**Development Dependencies:**
- `lombok` - Code generation (getters, setters, constructors)
- `spring-boot-starter-test` - Testing framework (JUnit, Mockito, AssertJ)

### 📊 Bağımlılık Özeti
| Servis | Web | Security | JPA | Eureka | Feign | AMQP | JWT | Actuator | Lombok |
|--------|-----|----------|-----|--------|-------|------|-----|----------|--------|
| API Gateway | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Auth Service | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ |
| Eureka Server | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Order Service | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Payment Service | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ | ✅ |
| Product Service | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ |

### 🎯 Özel Kütüphaneler ve Amaçları
- **JJWT (0.12.5)**: JWT token oluşturma, doğrulama ve parsing
- **OpenFeign**: Declarative HTTP client (Order → Product Service)
- **Resilience4j**: Circuit breaker pattern (Order Service)
- **Spring Cloud Gateway**: API Gateway ve routing
- **Spring Cloud LoadBalancer**: Client-side load balancing
- **Spring AMQP**: RabbitMQ messaging (Order ↔ Payment Service)
- **Spring Data JPA**: Database operations ve ORM
- **Spring Security**: Authentication ve authorization
- **Lombok**: Boilerplate code reduction

## 📝 Lisans

MIT License - see LICENSE file for details.

---

**İyi çalışmalar! 🚀**
