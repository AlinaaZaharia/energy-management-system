package com.a1.demo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SyncEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SyncEventPublisher.class);

    @Value("${sync.queue.name}")
    private String syncQueueName;

    private final RabbitTemplate rabbitTemplate;

    public SyncEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(SyncEvent event) {
        log.info("Publishing DEVICE sync event: action={}, deviceId={}",
                event.getActionType(), event.getEntityId());

        rabbitTemplate.convertAndSend(syncQueueName, event);
    }
}

