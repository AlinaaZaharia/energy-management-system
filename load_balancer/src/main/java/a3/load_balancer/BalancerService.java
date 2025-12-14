package a3.load_balancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BalancerService {

    private static final Logger log = LoggerFactory.getLogger(BalancerService.class);
    private final RabbitTemplate rabbitTemplate;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String[] replicaQueues = {"monitoring_q_1", "monitoring_q_2"};

    public BalancerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "device.measurements")
    public void distributeTraffic(Object messagePayload) {
        int index = Math.abs(counter.getAndIncrement()) % replicaQueues.length;
        String targetQueue = replicaQueues[index];
        log.info("LB: Redirecting message to replica queue -> {}", targetQueue);
        rabbitTemplate.convertAndSend(targetQueue, messagePayload);
    }
}