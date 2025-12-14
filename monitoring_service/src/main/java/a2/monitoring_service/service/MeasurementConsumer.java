package a2.monitoring_service.service;

import a2.monitoring_service.messaging.MeasurementMessage;
import a2.monitoring_service.model.HourlyConsumption;
import a2.monitoring_service.repository.HourlyConsumptionRepository;
import a2.monitoring_service.repository.SyncedDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class MeasurementConsumer {

    private static final Logger log = LoggerFactory.getLogger(MeasurementConsumer.class);
    private final HourlyConsumptionRepository hourlyConsumptionRepository;
    private final SyncedDeviceRepository deviceRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${notification.queue.name}")
    private String notificationQueue;

    @Value("${monitoring.queue.name:monitoring_q_1}")
    private String currentQueueName;

    public MeasurementConsumer(HourlyConsumptionRepository hourlyConsumptionRepository,
                               SyncedDeviceRepository deviceRepository,
                               RabbitTemplate rabbitTemplate) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${monitoring.queue.name:monitoring_q_1}")
    public void handleMeasurement(MeasurementMessage message) {
        log.info("Replica listening on [{}] received measurement from device: {}", currentQueueName, message.getDeviceId());

        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    message.getTimestamp(),
                    ZoneId.systemDefault()
            );
            LocalDateTime hourStart = dateTime.truncatedTo(ChronoUnit.HOURS);

            HourlyConsumption hourly = hourlyConsumptionRepository
                    .findByDeviceIdAndHourTimestamp(message.getDeviceId(), hourStart)
                    .orElseGet(() -> new HourlyConsumption(message.getDeviceId(), hourStart, 0.0));

            hourly.addToTotal(message.getMeasurementValue());
            hourlyConsumptionRepository.save(hourly);

            checkAndNotifyOverconsumption(message.getDeviceId(), hourly.getTotalConsumption());

        } catch (Exception e) {
            log.error("Error processing measurement: {}", e.getMessage(), e);
        }
    }

    private void checkAndNotifyOverconsumption(UUID deviceId, Double currentTotalConsumption) {
        deviceRepository.findById(deviceId).ifPresent(device -> {
            Double maxLimit = device.getMaxConsumption();

            if (maxLimit != null && currentTotalConsumption > maxLimit) {
                log.warn("ALERT: Device {} exceeded limit! Current: {}, Max: {}",
                        device.getName(), currentTotalConsumption, maxLimit);

                NotificationDTO notification = new NotificationDTO(
                        device.getUserId(),
                        device.getId(),
                        String.format("High energy usage detected for '%s'! Current: %.2f kWh (Limit: %.2f kWh)",
                                device.getName(), currentTotalConsumption, maxLimit)
                );
                rabbitTemplate.convertAndSend(notificationQueue, notification);
            }
        });
    }

    public record NotificationDTO(UUID userId, UUID deviceId, String message) {}
}