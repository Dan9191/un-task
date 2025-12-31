package ru.dan.hw.serviceredis.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Активная подписка")
public record ActiveSubscription(
        @Schema(description = "Дата активации")
        LocalDate activationDate,
        @Schema(description = "Дата истечения")
        LocalDate expiryDate,
        @Schema(description = "Стоимость")
        Integer price
) {}
