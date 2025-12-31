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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {

    private static final String RECEIPTS_KEY = "user:%s:receipts";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Обновляем данные кэша для конкретного пользователя.
     */
    @Override
    public void updateCache(ReceiptMessage message) {
        UUID userId = message.userId();
        try {
            ReceiptInfo receipt = new ReceiptInfo(
                    message.id(),
                    message.issueDate(),
                    message.activationDate(),
                    message.price()
            );

            redisTemplate.opsForZSet().add(
                    RECEIPTS_KEY.formatted(userId),
                    objectMapper.writeValueAsString(receipt),
                    message.activationDate().toEpochDay()
            );
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
     * Получение данных о чеках пользователя.
     */
    @Override
    public UserInfoResponse getUserInfo(UUID userId, int page, int size) {

        try {
            String receiptsKey = RECEIPTS_KEY.formatted(userId);

            // самый свежий счёт
            Set<String> latestRaw = redisTemplate.opsForZSet()
                    .reverseRange(receiptsKey, 0, 0);

            ActiveSubscription activeSubscription = null;

            if (latestRaw != null && !latestRaw.isEmpty()) {
                ReceiptInfo latestReceipt = objectMapper.readValue(
                        latestRaw.iterator().next(),
                        ReceiptInfo.class
                );

                LocalDate expiryDate = latestReceipt.activationDate().plusMonths(1);

                if (!expiryDate.isBefore(LocalDate.now())) {
                    activeSubscription = new ActiveSubscription(
                            latestReceipt.activationDate(),
                            expiryDate,
                            latestReceipt.price()
                    );
                }
            }

            // Пагинированные счета
            long start = (long) page * size;
            long end = start + size - 1;

            Set<String> receiptsRaw = redisTemplate.opsForZSet()
                    .reverseRange(receiptsKey, start, end);

            long total = Optional.ofNullable(
                    redisTemplate.opsForZSet().size(receiptsKey)
            ).orElse(0L);

            List<ReceiptInfo> receipts = receiptsRaw == null
                    ? List.of()
                    : receiptsRaw.stream()
                    .map(r -> {
                        try {
                            return objectMapper.readValue(r, ReceiptInfo.class);
                        } catch (JsonProcessingException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .toList();

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
}
