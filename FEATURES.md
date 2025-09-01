# Mikro Microservices - New Features

This document describes the newly implemented features in the Mikro microservices project.

## 🚀 Implemented Features

### 1. JWT Refresh Token System ✅

**Location**: `auth-service`

**Features**:
- **Access Tokens**: Short-lived (15 minutes) for API access
- **Refresh Tokens**: Long-lived (7 days) for token renewal
- **Automatic Cleanup**: Expired tokens are automatically removed
- **Token Revocation**: Users can logout and revoke tokens
- **Database Storage**: Refresh tokens are stored securely in the database

**New Endpoints**:
- `POST /api/v1/auth/refresh` - Refresh access token using refresh token
- `POST /api/v1/auth/logout` - Revoke refresh token

**Database Changes**:
- New `refresh_tokens` table with fields: id, token, username, expiry_date, created_at, revoked

**Configuration**:
```properties
jwt.access-token-expiration-minutes=15
jwt.refresh-token-expiration-days=7
```

### 2. Circuit Breaker Pattern ✅

**Location**: `order-service`

**Features**:
- **Resilience4j Integration**: Spring Cloud Circuit Breaker with Resilience4j
- **Fallback Responses**: Graceful degradation when product service is unavailable
- **Configurable Thresholds**: 50% failure rate threshold, 10-second wait time
- **Timeout Protection**: 5-second timeout for external calls

**Configuration**:
```properties
resilience4j.circuitbreaker.instances.product-service.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.product-service.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.product-service.sliding-window-size=10
resilience4j.circuitbreaker.instances.product-service.minimum-number-of-calls=5
resilience4j.timelimiter.instances.product-service.timeout-duration=5s
```

**Fallback Behavior**:
- When product service is unavailable, returns placeholder product data
- Prevents cascade failures in the order service

### 3. API Versioning ✅

**Location**: All services

**Features**:
- **URL Path Versioning**: `/api/v1/` prefix for all endpoints
- **Backward Compatibility**: Old endpoints still work (marked as deprecated)
- **Version Configuration**: Centralized versioning configuration
- **Future-Proof**: Easy to add v2, v3, etc.

**Versioned Endpoints**:
- Auth Service: `/api/v1/auth/*`
- Product Service: `/api/v1/products/*`
- Order Service: `/api/v1/orders/*`

**API Versioning**:
- Clean interface-based architecture
- All endpoints use `/api/v1/` prefix
- Easy to add future versions (v2, v3, etc.)

## 🔧 Technical Implementation Details

### JWT Refresh Token Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client App    │    │   Auth Service   │    │   Database      │
│                 │    │                  │    │                 │
│ 1. Login        │───▶│ Generate Tokens  │───▶│ Store Refresh   │
│ 2. Store Tokens │◀───│ Return Both      │    │ Token           │
│                 │    │                  │    │                 │
│ 3. API Call     │───▶│ Validate Access  │    │                 │
│ (Access Token)  │    │ Token            │    │                 │
│                 │    │                  │    │                 │
│ 4. Token Expired│───▶│ Refresh Endpoint │───▶│ Validate Refresh│
│ 5. Get New Token│◀───│ Generate New     │    │ Token           │
│                 │    │ Access Token     │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Circuit Breaker States

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    CLOSED   │───▶│    OPEN     │───▶│ HALF-OPEN   │
│             │    │             │    │             │
│ Normal      │    │ Failing     │    │ Testing     │
│ Operation   │    │ Fast        │    │ Recovery    │
│             │    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘
       ▲                                      │
       └──────────────────────────────────────┘
```

### API Versioning Strategy

```
Current Version (v1):
/api/v1/auth/login
/api/v1/products/{id}
/api/v1/orders

Future Version (v2):
/api/v2/auth/login
/api/v2/products/{id}
/api/v2/orders
```

## 🚀 Usage Examples

### JWT Refresh Token Flow

1. **Login**:
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "password"}'

# Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "550e8400-e29b-41d4-a716-446655440000",
  "token_type": "Bearer"
}
```

2. **Use Access Token**:
```bash
curl -X GET http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

3. **Refresh Token**:
```bash
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "550e8400-e29b-41d4-a716-446655440000"}'

# Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer"
}
```

4. **Logout**:
```bash
curl -X POST http://localhost:8081/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "550e8400-e29b-41d4-a716-446655440000"}'
```

### Circuit Breaker Testing

1. **Normal Operation**:
```bash
curl -X GET http://localhost:8083/api/v1/orders/1 \
  -H "Authorization: Bearer <access_token>"
```

2. **When Product Service is Down**:
- Circuit breaker opens after 5 failures
- Fallback response is returned
- No cascade failure to order service

### API Versioning

1. **Current Version**:
```bash
curl -X GET http://localhost:8082/api/v1/products/1
```

2. **Future Version** (when implemented):
```bash
curl -X GET http://localhost:8082/api/v2/products/1
```

## 🔧 Configuration

### Environment Variables

```bash
# JWT Secret (use strong secret in production)
JWT_SECRET=your-very-strong-secret-key-here

# Database URLs (if different from localhost)
AUTH_DB_URL=jdbc:postgresql://localhost:5432/auth_db
PRODUCT_DB_URL=jdbc:postgresql://localhost:5432/product_db
ORDER_DB_URL=jdbc:postgresql://localhost:5432/order_db
```

### Docker Compose

The existing `docker-compose.yml` supports all new features:
- PostgreSQL for database storage
- RabbitMQ for message queuing
- All services with proper networking

## 🧪 Testing

### Manual Testing

1. **Start Services**:
```bash
docker-compose up -d
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl product-service
mvn spring-boot:run -pl order-service
```

2. **Test JWT Flow**:
- Register a user
- Login to get tokens
- Use access token for API calls
- Refresh token when expired
- Logout to revoke tokens

3. **Test Circuit Breaker**:
- Stop product service
- Create orders (should get fallback responses)
- Restart product service
- Verify normal operation resumes

4. **Test API Versioning**:
- Use v1 endpoints
- Verify clean interface-based architecture

## 📊 Monitoring

### Health Checks

All services expose health endpoints:
- `/actuator/health` - Service health
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Circuit Breaker Metrics

Resilience4j provides metrics for:
- Circuit breaker state
- Failure rates
- Response times
- Fallback usage

## 🔒 Security Considerations

### JWT Security

- **Short-lived Access Tokens**: 15 minutes reduces exposure window
- **Secure Refresh Tokens**: Stored in database with expiration
- **Token Revocation**: Users can logout and revoke tokens
- **Automatic Cleanup**: Expired tokens are automatically removed

### API Security

- **Versioned Endpoints**: Clear separation of API versions
- **Interface-based Architecture**: Clean separation of concerns
- **Future-proof Design**: Easy to add new versions

## 🚀 Future Enhancements

### Potential Improvements

1. **Rate Limiting**: Add rate limiting to prevent abuse
2. **API Documentation**: OpenAPI/Swagger documentation
3. **Distributed Tracing**: Add tracing for request flow
4. **Metrics Dashboard**: Grafana dashboard for monitoring
5. **Load Testing**: Automated load testing for circuit breakers

### Version 2 API Considerations

- **Breaking Changes**: Plan for v2 API with breaking changes
- **Interface Evolution**: Add new methods to interfaces
- **Feature Flags**: Use feature flags for gradual rollouts

## 📝 Notes

- All new features are production-ready
- Existing functionality remains unchanged
- Database migrations are handled automatically by Hibernate
- Circuit breaker configuration can be tuned based on requirements
- JWT token expiration times can be adjusted per environment needs
- **Interface-based Architecture**: All controllers use interface pattern for better maintainability and testing
- **Clean Separation**: API contracts are defined in interfaces, implementations are separate classes
- **Versioned APIs**: Only v1 endpoints are available, future versions can be easily added
