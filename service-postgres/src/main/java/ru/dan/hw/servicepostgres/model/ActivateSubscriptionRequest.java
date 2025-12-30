package ru.dan.hw.servicepostgres.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Запрос на активацию подписки")
public record ActivateSubscriptionRequest(

        @Schema(
                description = "Идентификатор пользователя",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        UUID userId,

        @Schema(
                description = "Тип подписки",
                example = "BASIC",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Pattern(regexp = "BASIC|PRO")
        String subscriptionType,

        @Schema(
                description = "Дата активации подписки (yyyy-MM-dd)",
                example = "2025-01-01",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @FutureOrPresent
        LocalDate activationDate
) {}
