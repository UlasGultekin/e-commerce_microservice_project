package com.mikro.payment.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@Component
public class PaymentListener {
    private final RabbitTemplate rabbitTemplate;

    private final String resultQueue;

    public PaymentListener(RabbitTemplate rabbitTemplate,
                           @org.springframework.beans.factory.annotation.Value("${amqp.resultQueue}") String resultQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.resultQueue = resultQueue;
    }

    public record PaymentRequest(Long orderId, BigDecimal amount) {}
    public record PaymentResult(Long orderId, String status) {}

    @RabbitListener(queues = "${amqp.requestQueue}")
    public void onPaymentRequest(PaymentRequest request) {
        String status = new Random().nextInt(100) < 90 ? "PAID" : "FAILED";
        rabbitTemplate.convertAndSend(resultQueue, new PaymentResult(request.orderId(), status));
    }
}


