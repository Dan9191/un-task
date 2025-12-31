package ru.dan.hw.serviceredis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.dan.hw.serviceredis.model.ActiveSubscription;
import ru.dan.hw.serviceredis.model.ReceiptInfo;
import ru.dan.hw.serviceredis.model.ReceiptMessage;
import ru.dan.hw.serviceredis.model.UserInfoResponse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheServiceImpl implements UserCacheService {

    private static final String USER_RECEIPTS_KEY_PREFIX = "user:%s:receipts";
    private static final String USER_SUBSCRIPTION_KEY_PREFIX = "user:%s:subscription";
    private static final String USER_RECEIPTS_COUNT_KEY = "user:%s:receipts:count";
    private static final long RECEIPT_TTL_DAYS = 90; // TTL для кэша счетов (90 дней)
    private static final long SUBSCRIPTION_TTL_DAYS = 30; // TTL для подписки (30 дней)

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Обновляем данные кэша для конкретного пользователя.
     */
    @Override
    public void updateCache(ReceiptMessage message) {
        UUID userId = message.userId();

        try {
            // 1. Создаем ReceiptInfo из сообщения
            ReceiptInfo receipt = new ReceiptInfo(
                    message.id(),
                    message.issueDate(),
                    message.activationDate(),
                    message.price()
            );

            String userReceiptsKey = String.format(USER_RECEIPTS_KEY_PREFIX, userId);
            String receiptJson = objectMapper.writeValueAsString(receipt);

            double score = message.activationDate().toEpochDay();

            redisTemplate.opsForZSet().add(userReceiptsKey, receiptJson, score);
            redisTemplate.expire(userReceiptsKey, RECEIPT_TTL_DAYS, TimeUnit.DAYS);
            String userReceiptsCountKey = String.format(USER_RECEIPTS_COUNT_KEY, userId);
            redisTemplate.opsForValue().increment(userReceiptsCountKey);
            redisTemplate.expire(userReceiptsCountKey, RECEIPT_TTL_DAYS, TimeUnit.DAYS);

            updateUserSubscription(userId, receipt);

            log.info("Successfully updated cache for user '{}'", userId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize receipt for user '{}'", userId, e);
            throw new IllegalStateException("Failed to serialize receipt", e);
        } catch (Exception e) {
            log.error("Failed to update cache for user '{}'", userId, e);
            throw new IllegalStateException("Failed to update user cache", e);
        }
    }

    /**
     * Обновление активной подписки пользователя.
     */
    private void updateUserSubscription(UUID userId, ReceiptInfo latestReceipt) {
        try {
            LocalDate expiryDate = latestReceipt.activationDate().plusMonths(1);


            if (!expiryDate.isBefore(LocalDate.now())) {
                ActiveSubscription subscription = new ActiveSubscription(
                        latestReceipt.activationDate(),
                        expiryDate,
                        latestReceipt.price()
                );

                String subscriptionKey = String.format(USER_SUBSCRIPTION_KEY_PREFIX, userId);
                String subscriptionJson = objectMapper.writeValueAsString(subscription);

                redisTemplate.opsForValue().set(
                        subscriptionKey,
                        subscriptionJson,
                        SUBSCRIPTION_TTL_DAYS,
                        TimeUnit.DAYS
                );

                log.debug("Updated subscription for user '{}'", userId);
            }
        } catch (Exception e) {
            log.error("Failed to update subscription for user '{}'", userId, e);
        }
    }

    /**
     * Получение данных пользователя.
     */
    @Override
    public UserInfoResponse getUserInfo(UUID userId, int page, int size) {
        try {

            ActiveSubscription activeSubscription = getUserSubscription(userId);

            String userReceiptsKey = String.format(USER_RECEIPTS_KEY_PREFIX, userId);

            long start = (long) page * size;
            long end = start + size - 1;

            Set<String> receiptsRaw = redisTemplate.opsForZSet()
                    .reverseRange(userReceiptsKey, start, end);

            String userReceiptsCountKey = String.format(USER_RECEIPTS_COUNT_KEY, userId);
            String countStr = redisTemplate.opsForValue().get(userReceiptsCountKey);
            long total = countStr != null ? Long.parseLong(countStr) : 0L;

            List<ReceiptInfo> receipts = receiptsRaw == null || receiptsRaw.isEmpty()
                    ? Collections.emptyList()
                    : receiptsRaw.stream()
                    .map(this::parseReceiptJson)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved cache for user '{}'", userId);
            return new UserInfoResponse(
                    userId,
                    activeSubscription,
                    receipts,
                    total
            );

        } catch (RedisConnectionFailureException ex) {
            log.error("Redis connection failed for user '{}'", userId, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to retrieve cache for user '{}'", userId, ex);
            throw new IllegalStateException("Failed to read user cache", ex);
        }
    }

    /**
     * Получение активной подписки пользователя.
     */
    private ActiveSubscription getUserSubscription(UUID userId) {
        try {
            String subscriptionKey = String.format(USER_SUBSCRIPTION_KEY_PREFIX, userId);
            String subscriptionJson = redisTemplate.opsForValue().get(subscriptionKey);

            if (subscriptionJson != null && !subscriptionJson.isEmpty()) {
                ActiveSubscription subscription = objectMapper.readValue(
                        subscriptionJson,
                        ActiveSubscription.class
                );

                if (!subscription.expiryDate().isBefore(LocalDate.now())) {
                    return subscription;
                } else {
                    redisTemplate.delete(subscriptionKey);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get subscription for user '{}'", userId, e);
        }
        return null;
    }

    /**
     * Парсинг JSON в ReceiptInfo.
     */
    private ReceiptInfo parseReceiptJson(String receiptJson) {
        try {
            return objectMapper.readValue(receiptJson, ReceiptInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse receipt JSON: {}", receiptJson, e);
            return null;
        }
    }

}
