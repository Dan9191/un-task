package ru.dan.hw.servicepostgres.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@Schema(description = "Запрос на деактивацию подписки")
public record DeactivateSubscriptionRequest(

        @Schema(
                description = "Идентификатор пользователя",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        UUID userId,

        @Schema(
                description = "Тип подписки",
                example = "PRO",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Pattern(regexp = "BASIC|PRO")
        String subscriptionType
) {}