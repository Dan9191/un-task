package ru.dan.hw.serviceredis.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Информация о счете")
public record ReceiptInfo(
        @Schema(description = "ID счета")
        Integer receiptId,
        @Schema(description = "Дата обработки")
        LocalDate issueDate,
        @Schema(description = "Дата активации")
        LocalDate activationDate,
        @Schema(description = "Стоимость")
        Integer price
) {}