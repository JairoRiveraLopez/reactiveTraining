package com.transactions.banking_demo.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setReplyTimeout(5000);
        template.setUseTemporaryReplyQueues(true);
        return template;
    }

    @Bean
    public DirectExchange ledgerExchange() {
        return new DirectExchange("ledger.exchange");
    }

    @Bean
    public Queue requestQueue() {
        return new Queue("ledger.entry.request.queue", true);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(requestQueue())
                .to(ledgerExchange())
                .with("ledger.entry.request");
    }

}
