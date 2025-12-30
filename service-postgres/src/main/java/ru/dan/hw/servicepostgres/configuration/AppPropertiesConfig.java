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

    /**
     * Топик для отправки обработанных чеков.
     */
    private String billingQueue = "billing.checks.queue";

    /**
     * Cron-выражение для ежедневной задачи генерация чеков.
     */
    private String dailyCron = "0 5 17 * * *";

    /**
     * Задержка (в миллисекундах) между запусками задачи отправки чеков в брокер.
     */
    private long retryDelay = 30_000L;

}