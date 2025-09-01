package com.mikro.order.amqp;

import com.mikro.order.domain.Order;
import com.mikro.order.domain.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultListener {
    private final OrderRepository orderRepository;

    public PaymentResultListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @RabbitListener(queues = "${amqp.resultQueue}")
    public void onPaymentResult(PaymentEvents.PaymentResult result) {
        orderRepository.findById(result.orderId()).ifPresent(order -> {
            order.setStatus("PAID".equalsIgnoreCase(result.status()) ? "PAID" : "FAILED");
            orderRepository.save(order);
        });
    }
}


