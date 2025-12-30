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

        log.info("Attempting to activate subscription for user {} of type {} with activation date {}",
                userId, subscriptionTypeName, activationDate);

        if (activationDate.isBefore(LocalDate.now())) {
            log.warn("Activation date {} is in the past for user {}", activationDate, userId);
            throw new IllegalArgumentException("Activation date cannot be in the past");
        }

        SubscriptionType type = subscriptionTypeService.findByName(subscriptionTypeName)
                .orElseThrow(() -> {
                    log.error("Unknown subscription type: {}", subscriptionTypeName);
                    return new IllegalArgumentException("Subscription type not found: " + subscriptionTypeName);
                });

        List<Subscription> activeSubs = subscriptionRepository.findByUserIdAndActiveTrue(userId);
        if (!activeSubs.isEmpty()) {
            log.warn("User {} already has an active subscription", userId);
            throw new IllegalStateException("User already has an active subscription");
        }

        userRepository.findById(userId).orElseGet(() -> {
            log.info("Creating new user with ID {}", userId);
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

        log.info("Subscription successfully activated: ID={}, user={}, type={}, date={}",
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

        log.info("Attempting to deactivate subscription of type {} for user {}", subscriptionTypeName, userId);

        SubscriptionType type = subscriptionTypeService.findByName(subscriptionTypeName)
                .orElseThrow(() -> {
                    log.error("Unknown subscription type during deactivation: {}", subscriptionTypeName);
                    return new IllegalArgumentException("Subscription type not found: " + subscriptionTypeName);
                });

        Subscription subscription = subscriptionRepository
                .findByUserIdAndSubscriptionTypeAndActiveTrue(userId, type)
                .orElseThrow(() -> {
                    log.warn("Active subscription of type {} not found for user {}", subscriptionTypeName, userId);
                    return new IllegalStateException(
                            "Active subscription of type " + subscriptionTypeName + " not found for user");
                });

        subscription.setActive(false);
        subscriptionRepository.save(subscription);

        log.info("Subscription deactivated: ID={}, user={}, type={}",
                subscription.getId(), userId, subscriptionTypeName);
    }
}
