package ru.dan.hw.servicepostgres.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация приложения.
 */
@Configuration
@ConfigurationProperties(prefix = "app.un")
@Data
public class AppPropertiesConfig {

    private String baseUrl;

    /**
     * Топик для отправки обработанных счетов.
     */
    private String billingQueue = "billing.checks.queue";

    /**
     * Размер обрабатываемой пачки счетов.
     */
    private int batchSize = 100;


}