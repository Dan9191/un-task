package ru.dan.hw.serviceredis.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сообщение с данными оплаты для принятия из брокера.
 *
 * @param id             ID чека.
 * @param userId         ID пользователя.
 * @param issueDate      Дата обработки.
 * @param activationDate Дата активации подписки.
 * @param price          Стоимость подписки.
 */
public record ReceiptMessage(
        Integer id,
        UUID userId,
        LocalDate issueDate,
        LocalDate activationDate,
        Integer price
) {}
