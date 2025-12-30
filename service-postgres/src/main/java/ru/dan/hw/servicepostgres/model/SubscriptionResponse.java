package ru.dan.hw.servicepostgres.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Информация о подписке")
public record SubscriptionResponse(

        @Schema(description = "Идентификатор подписки")
        Integer id,

        @Schema(description = "Идентификатор пользователя")
        UUID userId,

        @Schema(description = "Тип подписки")
        String subscriptionType,

        @Schema(description = "Дата активации")
        LocalDate activationDate
) {}
