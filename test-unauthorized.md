# Authentication Test Senaryoları

## Senaryo 1: Login Olmadan Product Update

```bash
# 1. Doğrudan 401 alma
curl -v -X PUT http://localhost:8082/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Hacked Product", "stock": 999, "price": 1.00}'

# Beklenen Response:
# HTTP/1.1 401 Unauthorized
# {"timestamp":"...","status":401,"error":"Unauthorized","path":"/api/products/1"}
```

## Senaryo 2: Geçersiz Token ile Product Update

```bash
# 2. Fake/Expired token ile
curl -v -X PUT http://localhost:8082/api/v1/products/1 \
  -H "Authorization: Bearer fake-invalid-token" \
  -H "Content-Type: application/json" \
  -d '{"name": "Hacked Product", "stock": 999, "price": 1.00}'

# Beklenen Response: 
# HTTP/1.1 401 Unauthorized (JWT parse fail)
```

## Senaryo 3: Customer Token ile Product Update (403)

```bash
# 3. Customer token ile (yetki yok)
# Önce customer token al
CUSTOMER_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer_bob", "password": "bob123"}' | jq -r '.access_token')

# Product update dene
curl -v -X PUT http://localhost:8082/api/v1/products/1 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Customer Hack", "stock": 999, "price": 1.00}'

# Beklenen Response:
# HTTP/1.1 403 Forbidden (Role check fail: ROLE_CUSTOMER != ROLE_SHOP_OWNER)
```

## Senaryo 4: Shop Owner ama Başka Kullanıcının Ürünü (403)

```bash
# 4. Başka shop owner'ın ürününü güncelleme
# İkinci shop owner token al
SHOP2_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "shop_veli", "password": "veli123"}' | jq -r '.access_token')

# İlk shop owner'ın ürününü güncellemeye çalış
curl -v -X PUT http://localhost:8082/api/v1/products/1 \
  -H "Authorization: Bearer $SHOP2_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Not My Product", "stock": 999, "price": 1.00}'

# Beklenen Response:
# HTTP/1.1 403 Forbidden
# {"error": "forbidden_not_owner"}
```

## Senaryo 5: Doğru Süreç

```bash
# 5. Doğru shop owner ile update
SHOP_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "shop_alice", "password": "alice123"}' | jq -r '.access_token')

curl -v -X PUT http://localhost:8082/api/v1/products/1 \
  -H "Authorization: Bearer $SHOP_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated iPhone 15", "stock": 20, "price": 1099.99}'

# Beklenen Response:
# HTTP/1.1 200 OK
# {"id":1,"name":"Updated iPhone 15","stock":20,"price":1099.99,"ownerUsername":"shop_alice"}
```
