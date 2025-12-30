package ru.dan.hw.servicepostgres.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.dan.hw.servicepostgres.entity.Receipt;
import ru.dan.hw.servicepostgres.entity.Subscription;
import ru.dan.hw.servicepostgres.entity.SubscriptionType;
import ru.dan.hw.servicepostgres.repository.ReceiptRepository;
import ru.dan.hw.servicepostgres.repository.SubscriptionRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Сервис обработки подписок.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final ReceiptRepository receiptRepository;


    /**
     * Задача для выписывания счетов на активные и не обработанные подписки.
     */
    @Scheduled(cron = "#{@appPropertiesConfig.dailyCron}")
    @Transactional
    public void generateDailyReceipts() {
        LocalDate today = LocalDate.now();

        log.info("Запуск ежедневного биллинга на {}", today);

        List<Subscription> subscriptionsToBill = subscriptionRepository
                .findAllByActivationDateAndActiveTrueAndProcessedFalse(today);

        if (subscriptionsToBill.isEmpty()) {
            log.info("Нет подписок для биллинга сегодня");
            return;
        }

        log.info("Найдено {} подписок для выставления счёта", subscriptionsToBill.size());

        for (Subscription sub : subscriptionsToBill) {
            SubscriptionType type = sub.getSubscriptionType();

            Receipt receipt = Receipt.builder()
                    .userId(sub.getUserId())
                    .issueDate(today)
                    .activationDate(sub.getActivationDate())
                    .subscriptionType(type)
                    .build();

            receiptRepository.save(receipt);

            sub.setProcessed(true);
            subscriptionRepository.save(sub);

            log.info("Счёт создан и сохранён в очередь: user={}, type={}, activationDate={}",
                    sub.getUserId(), type.getName(), sub.getActivationDate());
        }

        log.info("Генерация счетов завершена");
    }
}
