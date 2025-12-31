package ru.dan.hw.serviceredis.service;

import ru.dan.hw.serviceredis.model.ReceiptInfo;
import ru.dan.hw.serviceredis.model.ReceiptMessage;
import ru.dan.hw.serviceredis.model.UserInfoResponse;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс для реализаций методов кэширования.
 */
public interface UserCacheService {

    /**
     * Обновление кэша данными из сообщения.
     */
    void updateCache(ReceiptMessage message);

    /**
     * Получение информации о пользователе с пагинацией.
     */
    UserInfoResponse getUserInfo(UUID userId, int page, int size);


}