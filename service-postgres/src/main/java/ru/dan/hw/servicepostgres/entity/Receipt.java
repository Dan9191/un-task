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
 * Счет.
 */
@Entity
@Table(name = "receipt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Дата выставления счета.
     */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /**
     * Дата активации отчета.
     */
    @Column(name = "activation_date")
    private LocalDate activationDate;

    /**
     * Тип подписки.
     */
    @ManyToOne
    @JoinColumn(name = "subscription_type_id", nullable = false)
    private SubscriptionType subscriptionType;

    /**
     * Отправлена ли в брокер сообщений.
     */
    @Column(name = "sent_to_broker")
    private boolean sentToBroker;
}
