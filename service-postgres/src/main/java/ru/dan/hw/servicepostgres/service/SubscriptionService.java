package ru.dan.hw.servicepostgres.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dan.hw.servicepostgres.entity.Subscription;
import ru.dan.hw.servicepostgres.entity.SubscriptionType;
import ru.dan.hw.servicepostgres.entity.User;
import ru.dan.hw.servicepostgres.model.ActivateSubscriptionRequest;
import ru.dan.hw.servicepostgres.model.DeactivateSubscriptionRequest;
import ru.dan.hw.servicepostgres.model.SubscriptionResponse;
import ru.dan.hw.servicepostgres.repository.SubscriptionRepository;
import ru.dan.hw.servicepostgres.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Сервис работы с подписками.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    /**
     * Репозиторий работы с подписками.
     */
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Репозиторий работы с пользователями.
     */
    private final UserRepository userRepository;

    /**
     * Сервис работы с типами подписками.
     */
    private final SubscriptionTypeService subscriptionTypeService;

    /**
     * Активация новой подписки.
     *
     * @param request Данные запроса на активацию.
     * @return результат активации.
     */
    @Transactional
    public SubscriptionResponse activateSubscription(ActivateSubscriptionRequest request) {
        UUID userId = request.userId();
        String subscriptionTypeName = request.subscriptionType();
        LocalDate activationDate = request.activationDate();

        log.info("Попытка активации подписки для пользователя {} типа {} с датой активации {}",
                userId, subscriptionTypeName, activationDate);

        if (activationDate.isBefore(LocalDate.now())) {
            log.warn("Дата активации {} в прошлом для пользователя {}", activationDate, userId);
            throw new IllegalArgumentException("Дата активации не может быть в прошлом");
        }

        SubscriptionType type = subscriptionTypeService.findByName(subscriptionTypeName)
                .orElseThrow(() -> {
                    log.error("Неизвестный тип подписки: {}", subscriptionTypeName);
                    return new IllegalArgumentException("Тип подписки не найден: " + subscriptionTypeName);
                });

        List<Subscription> activeSubs = subscriptionRepository.findByUserIdAndActiveTrue(userId);
        if (!activeSubs.isEmpty()) {
            log.warn("У пользователя {} уже есть активная подписка", userId);
            throw new IllegalStateException("У пользователя уже есть активная подписка");
        }

        userRepository.findById(userId).orElseGet(() -> {
            log.info("Создаём нового пользователя с ID {}", userId);
            User newUser = User.builder().userId(userId).build();
            return userRepository.save(newUser);
        });

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .subscriptionType(type)
                .activationDate(activationDate)
                .active(true)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        log.info("Подписка успешно активирована: ID={}, пользователь={}, тип={}, дата={}",
                saved.getId(), userId, subscriptionTypeName, activationDate);

        return new SubscriptionResponse(
                saved.getId(),
                saved.getUserId(),
                type.getName(),
                saved.getActivationDate()
        );
    }

    /**
     * Деактивация подписки.
     *
     * @param request Запрос на деактивацию.
     */
    @Transactional
    public void deactivateSubscription(DeactivateSubscriptionRequest request) {
        UUID userId = request.userId();
        String subscriptionTypeName = request.subscriptionType();

        log.info("Попытка деактивации подписки типа {} для пользователя {}", subscriptionTypeName, userId);

        SubscriptionType type = subscriptionTypeService.findByName(subscriptionTypeName)
                .orElseThrow(() -> {
                    log.error("Неизвестный тип подписки при деактивации: {}", subscriptionTypeName);
                    return new IllegalArgumentException("Тип подписки не найден: " + subscriptionTypeName);
                });

        Subscription subscription = subscriptionRepository
                .findByUserIdAndSubscriptionTypeAndActiveTrue(userId, type)
                .orElseThrow(() -> {
                    log.warn("Активная подписка типа {} не найдена для пользователя {}", subscriptionTypeName, userId);
                    return new IllegalStateException(
                            "Активная подписка типа " + subscriptionTypeName + " не найдена у пользователя");
                });

        subscription.setActive(false);
        subscriptionRepository.save(subscription);

        log.info("Подписка деактивирована: ID={}, пользователь={}, тип={}",
                subscription.getId(), userId, subscriptionTypeName);
    }
}
