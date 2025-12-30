package ru.dan.hw.servicepostgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dan.hw.servicepostgres.entity.Subscription;
import ru.dan.hw.servicepostgres.entity.SubscriptionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {


    List<Subscription> findByUserIdAndActiveTrue(UUID userId);

    Optional<Subscription> findByUserIdAndSubscriptionTypeAndActiveTrue(
            UUID userId, SubscriptionType subscriptionType);

    /**
     * Поиск подписок с по дате активации, статусом подписки active = true и processed = false.
     *
     * @param activationDate Дата активации.
     * @return список подписок.
     */
    List<Subscription> findAllByActivationDateAndActiveTrueAndProcessedFalse(LocalDate activationDate);
}
