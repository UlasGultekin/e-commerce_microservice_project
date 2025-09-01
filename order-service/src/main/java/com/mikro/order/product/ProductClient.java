package com.mikro.order.product;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
public class ProductClient {
    
    private final ProductClientFeign productClientFeign;
    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;
    
    public ProductClient(ProductClientFeign productClientFeign, 
                        Resilience4JCircuitBreakerFactory circuitBreakerFactory) {
        this.productClientFeign = productClientFeign;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }
    
    public ProductResponse getProduct(Long id) {
        return circuitBreakerFactory.create("product-service")
                .run(() -> productClientFeign.getProduct(id),
                     throwable -> {
                         // Fallback response when circuit breaker is open
                         return ProductResponse.builder()
                                 .id(id)
                                 .name("Product temporarily unavailable")
                                 .price(0.0)
                                 .stock(0)
                                 .build();
                     });
    }
    
    public ProductResponse reduceStock(Long id, StockReductionRequest request) {
        return circuitBreakerFactory.create("product-service")
                .run(() -> productClientFeign.reduceStock(id, request),
                     throwable -> {
                         // Fallback response when circuit breaker is open
                         return ProductResponse.builder()
                                 .id(id)
                                 .name("Stock reduction failed - service unavailable")
                                 .price(0.0)
                                 .stock(0)
                                 .build();
                     });
    }
    
    @FeignClient(name = "product-service")
    interface ProductClientFeign {
        @GetMapping("/api/v1/products/{id}")
        ProductResponse getProduct(@PathVariable("id") Long id);
        
        @PostMapping("/api/v1/products/{id}/reduce-stock")
        ProductResponse reduceStock(@PathVariable("id") Long id, @RequestBody StockReductionRequest request);
    }
}


