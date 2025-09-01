package com.mikro.order.amqp;

import java.math.BigDecimal;

public class PaymentEvents {
    public record PaymentRequest(Long orderId, BigDecimal amount) {}
    public record PaymentResult(Long orderId, String status) {}
}


