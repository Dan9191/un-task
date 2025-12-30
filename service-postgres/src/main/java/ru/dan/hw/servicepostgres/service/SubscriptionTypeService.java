package ru.dan.hw.servicepostgres.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dan.hw.servicepostgres.entity.SubscriptionType;
import ru.dan.hw.servicepostgres.repository.SubscriptionTypeRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис работы с типами подписок.
 */
@Service
public class SubscriptionTypeService {

    /**
     * Репозиторий типов подписок.
     */
    private final SubscriptionTypeRepository subscriptionTypeRepository;


    /**
     * Кэш типов подписок.
     */
    private final Map<String, SubscriptionType> subscriptionTypeCache;

    @Autowired
    public SubscriptionTypeService(SubscriptionTypeRepository subscriptionTypeRepository) {
        this.subscriptionTypeRepository = subscriptionTypeRepository;
        this.subscriptionTypeCache = new HashMap<>();
    }

    /**
     * Получает сущность типа подписки по названию.
     *
     * @param name Название типа подписки.
     * @return Тип подписки.
     */
    public Optional<SubscriptionType> findByName(String name) {
        fillCache();
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        SubscriptionType value = subscriptionTypeCache.get(name);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private void fillCache() {
        if (subscriptionTypeCache.isEmpty()) {
            subscriptionTypeCache.putAll(
                    subscriptionTypeRepository.findAll()
                            .stream()
                            .collect(Collectors.toMap(SubscriptionType::getName, Function.identity()))
            );
        }
    }
}
