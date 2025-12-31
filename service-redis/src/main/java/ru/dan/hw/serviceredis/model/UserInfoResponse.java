package ru.dan.hw.serviceredis.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Информация о подписке")
public record UserInfoResponse(
        UUID userId,
        @Schema(description = "Информация о подписке")
        ActiveSubscription activeSubscriptions,
        @Schema(description = "Информация о подписке")
        List<ReceiptInfo> receipts,
        @Schema(description = "Информация о подписке")
        long totalReceipts
) {}
