package ru.dan.hw.servicepostgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Подписки.
 */
@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(
            name = "subscription_type_id",
            nullable = false
    )
    private SubscriptionType subscriptionType;

    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    /**
     * Активна ли подписка.
     */
    @Column(name = "active")
    private boolean active;

    /**
     * Обработана ли подписка.
     */
    @Column(name = "processed")
    private boolean processed;
}
