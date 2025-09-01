package com.mikro.order.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {
    @Bean
    public TopicExchange paymentExchange(@Value("${amqp.exchange}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue paymentRequestQueue(@Value("${amqp.requestQueue}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Queue paymentResultQueue(@Value("${amqp.resultQueue}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Binding paymentBinding(TopicExchange paymentExchange, Queue paymentRequestQueue,
                                  @Value("${amqp.routingKey}") String routingKey) {
        return BindingBuilder.bind(paymentRequestQueue).to(paymentExchange).with(routingKey);
    }
}


