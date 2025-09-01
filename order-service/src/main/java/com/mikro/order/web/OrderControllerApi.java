package com.mikro.order.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

public interface OrderControllerApi {

    @PostMapping
    ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest req);

    @GetMapping("/{id}")
    ResponseEntity<?> get(@PathVariable Long id);

    @GetMapping
    ResponseEntity<List<OrderResponse>> getMyOrders();

    record CreateOrderRequest(@NotEmpty List<OrderItemRequest> items) {}
    record OrderItemRequest(@Min(1) Long productId, @Min(1) int quantity) {}
    
    class OrderResponse {
        public Long id;
        public String customerUsername;
        public List<OrderItemResponse> items;
        public BigDecimal totalAmount;
        public String status;

        public OrderResponse() {}

        public OrderResponse(Long id, String customerUsername, List<OrderItemResponse> items, 
                           BigDecimal totalAmount, String status) {
            this.id = id;
            this.customerUsername = customerUsername;
            this.items = items;
            this.totalAmount = totalAmount;
            this.status = status;
        }

        // Constructor for Order domain object
        public OrderResponse(com.mikro.order.domain.Order order) {
            this.id = order.getId();
            this.customerUsername = order.getCustomerUsername();
            this.items = order.getItems().stream().map(OrderItemResponse::new).toList();
            this.totalAmount = order.getTotalAmount();
            this.status = order.getStatus();
        }
    }

    class OrderItemResponse {
        public Long productId;
        public String productName;
        public int quantity;
        public BigDecimal unitPrice;
        public BigDecimal totalPrice;

        public OrderItemResponse() {}

        public OrderItemResponse(Long productId, String productName, int quantity, 
                               BigDecimal unitPrice, BigDecimal totalPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        // Constructor for OrderItem domain object
        public OrderItemResponse(com.mikro.order.domain.OrderItem item) {
            this.productId = item.getProductId();
            this.productName = item.getProductName();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
            this.totalPrice = item.getTotalPrice();
        }
    }
}


