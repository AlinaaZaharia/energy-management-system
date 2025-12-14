package com.a1.demo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SyncEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SyncEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${sync.queue.name}")
    private String syncQueueName;

    public SyncEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserCreated(SyncEvent event) {
        log.info("Publishing USER_CREATED sync event for userId={}", event.getEntityId());
        rabbitTemplate.convertAndSend(syncQueueName, event);
    }

    public void publishUserUpdated(SyncEvent event) {
        log.info("Publishing USER_UPDATED sync event for userId={}", event.getEntityId());
        rabbitTemplate.convertAndSend(syncQueueName, event);
    }

    public void publishUserDeleted(SyncEvent event) {
        log.info("Publishing USER_DELETED sync event for userId={}", event.getEntityId());
        rabbitTemplate.convertAndSend(syncQueueName, event);
    }
}
