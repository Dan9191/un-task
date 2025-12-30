package ru.dan.hw.servicepostgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dan.hw.servicepostgres.entity.SubscriptionType;

public interface SubscriptionTypeRepository extends JpaRepository<SubscriptionType, Integer> {
}
