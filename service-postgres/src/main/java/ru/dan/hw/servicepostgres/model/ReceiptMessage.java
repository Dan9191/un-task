package ru.dan.hw.servicepostgres.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сообщение с данными оплаты для отправки в брокер
 *
 * @param id             ID чека.
 * @param userId         ID пользователя.
 * @param issueDate      Дата обработки.
 * @param activationDate Дата активации подписки.
 * @param price          Цена подписки.
 */
public record ReceiptMessage(
        Integer id,
        UUID userId,
        LocalDate issueDate,
        LocalDate activationDate,
        Integer price
) {}
