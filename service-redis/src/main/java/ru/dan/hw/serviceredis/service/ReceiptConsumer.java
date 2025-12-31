package ru.dan.hw.serviceredis.service;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import ru.dan.hw.serviceredis.model.ReceiptMessage;

import java.io.IOException;

/**
 * Слушатель брокера сообщений.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptConsumer {

    private final UserCacheService userCacheService;

    @RabbitListener(
            queues = "${app.un-redis.billing-queue}",
            containerFactory = "manualAckContainerFactory"
    )
    public void consume(
            ReceiptMessage message,
            Channel channel,
            Message amqpMessage
    ) throws IOException {

        try {
            log.info("receipt: {}", message);
            userCacheService.updateCache(message);
            channel.basicAck(
                    amqpMessage.getMessageProperties().getDeliveryTag(),
                    false
            );
        } catch (RedisConnectionFailureException ex) {
            channel.basicNack(
                    amqpMessage.getMessageProperties().getDeliveryTag(),
                    false,
                    true
            );
        }
    }
}

