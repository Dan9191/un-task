package ru.dan.hw.serviceredis.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация приложения.
 */
@Configuration
@ConfigurationProperties(prefix = "app.un-redis")
@Data
public class AppPropertiesConfig {

    /**
     * Топик для принятия обработанных чеков.
     */
    private String billingQueue = "billing.checks.queue";


}