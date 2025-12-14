package a3.communication_service.service;

import a3.communication_service.model.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "${notification.queue.name:notification.queue}")
    public void handleNotification(NotificationMessage notification) {
        log.info("Received notification for user {}: {}", notification.getUserId(), notification.getMessage());

        messagingTemplate.convertAndSend(
                "/topic/notifications/" + notification.getUserId(),
                notification
        );
    }
}