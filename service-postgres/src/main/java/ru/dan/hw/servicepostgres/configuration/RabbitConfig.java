package ru.dan.hw.servicepostgres.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final AppPropertiesConfig appPropertiesConfig;

    @Bean
    public Queue billingQueue() {
        return QueueBuilder
                .durable(appPropertiesConfig.getBillingQueue())
                .build();
    }

}