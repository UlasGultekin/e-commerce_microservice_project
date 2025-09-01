package com.mikro.order.web;

import com.mikro.order.amqp.PaymentEvents;
import com.mikro.order.domain.Order;
import com.mikro.order.domain.OrderItem;
import com.mikro.order.domain.OrderRepository;
import com.mikro.order.product.ProductClient;
import com.mikro.order.product.ProductResponse;
import com.mikro.order.product.StockReductionRequest;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderControllerApi {
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final AmqpTemplate amqpTemplate;
    private final String exchange;
    private final String routingKey;

    public OrderController(ProductClient productClient,
                           OrderRepository orderRepository,
                           AmqpTemplate amqpTemplate,
                           @Value("${amqp.exchange}") String exchange,
                           @Value("${amqp.routingKey}") String routingKey) {
        this.productClient = productClient;
        this.orderRepository = orderRepository;
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public ResponseEntity<?> create(CreateOrderRequest req) {
        String currentUser = String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        Order order = Order.builder()
            .customerUsername(currentUser)
            .status("CREATED")
            .build();

        // Process each item with stock reduction
        for (OrderItemRequest itemReq : req.items()) {
            try {
                // First get product details
                ProductResponse product = productClient.getProduct(itemReq.productId());
                if (product == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "product_not_found", 
                        "productId", itemReq.productId()
                    ));
                }

                // Reduce stock atomically
                ProductResponse updatedProduct = productClient.reduceStock(
                    itemReq.productId(), 
                    new StockReductionRequest(itemReq.quantity())
                );

                BigDecimal itemTotal = product.price().multiply(BigDecimal.valueOf(itemReq.quantity()));
                OrderItem orderItem = OrderItem.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .quantity(itemReq.quantity())
                    .unitPrice(product.price())
                    .totalPrice(itemTotal)
                    .build();
                
                order.addItem(orderItem);
            } catch (Exception ex) {
                // If stock reduction fails, return error
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "stock_reduction_failed", 
                    "productId", itemReq.productId(),
                    "message", ex.getMessage()
                ));
            }
        }

        order.calculateTotalAmount();
        order = orderRepository.save(order);

        amqpTemplate.convertAndSend(exchange, routingKey, 
            new PaymentEvents.PaymentRequest(order.getId(), order.getTotalAmount()));
        
        return ResponseEntity.ok(new OrderResponse(order));
    }

    @Override
    public ResponseEntity<?> get(Long id) {
        String currentUser = String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        return orderRepository.findByIdAndCustomerUsernameWithItems(id, currentUser)
            .<ResponseEntity<?>>map(order -> ResponseEntity.ok(new OrderResponse(order)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        String currentUser = String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        List<Order> orders = orderRepository.findByCustomerUsername(currentUser);
        List<OrderResponse> responses = orders.stream().map(OrderResponse::new).toList();
        return ResponseEntity.ok(responses);
    }

}


