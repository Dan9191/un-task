package ru.dan.hw.servicepostgres.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.dan.hw.servicepostgres.configuration.AppPropertiesConfig;
import ru.dan.hw.servicepostgres.entity.Receipt;
import ru.dan.hw.servicepostgres.model.ReceiptMessage;
import ru.dan.hw.servicepostgres.repository.ReceiptRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckRetryService {

    private final ReceiptRepository receiptRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AppPropertiesConfig appPropertiesConfig;

    /**
     * Периодическая задача повторной отправки счетов в брокер сообщений.
     */
    @Scheduled(fixedDelayString = "#{@appPropertiesConfig.retryDelay}")
    public void retrySendingChecks() {

        List<Receipt> unsentChecks = receiptRepository.findTop100BySentToBrokerFalseOrderById();

        if (unsentChecks.isEmpty()) {
            log.debug("Нет счетов для повторной отправки");
            return;
        }

        log.info("Найдено {} счетов для повторной отправки", unsentChecks.size());

        for (Receipt receipt : unsentChecks) {
            try {
                ReceiptMessage message = new ReceiptMessage(
                        receipt.getId(),
                        receipt.getUserId(),
                        receipt.getIssueDate(),
                        receipt.getActivationDate(),
                        receipt.getSubscriptionType().getPrice()
                );

                rabbitTemplate.convertAndSend(appPropertiesConfig.getBillingQueue(), message);

                int updated = receiptRepository.markAsSent(receipt.getId());
                if (updated == 0) {
                    log.warn("Счет {} уже был обработан параллельно", receipt.getId());
                } else {
                    log.info("Счет {} успешно отправлен в RabbitMQ", receipt.getId());
                }

            } catch (AmqpException e) {
                log.warn(
                        "RabbitMQ недоступен, остановка обработки. Последний счет id={}",
                        receipt.getId(),
                        e
                );
                break;
            } catch (Exception e) {
                log.error("Ошибка обработки счета id={}", receipt.getId(), e);
            }
        }

        log.info("Цикл повторной отправки завершён");
    }
}
