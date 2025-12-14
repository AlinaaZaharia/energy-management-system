package a2.device_simulator.service;

import a2.device_simulator.model.MeasurementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.UUID;

@Service
public class MeasurementProducer {

    private static final Logger log = LoggerFactory.getLogger(MeasurementProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    @Value("${simulator.device.id}")
    private String deviceId;

    @Value("${measurements.queue.name}")
    private String queueName;

    @Value("${simulator.seed-full-day:false}")
    private boolean seedFullDay;

    private double baseLoad = 0.5;

    public MeasurementProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    @Scheduled(fixedRateString = "${simulator.interval.millis:600000}")
    public void generateAndSendMeasurement() {
        try {
            int currentHour = java.time.LocalTime.now().getHour();
            double consumption = generateRealisticConsumptionForHour(currentHour);

            MeasurementMessage message = new MeasurementMessage(
                    UUID.fromString(deviceId),
                    consumption,
                    Instant.now()
            );

            rabbitTemplate.convertAndSend(queueName, message);

            log.info("Sent measurement: deviceId={}, value={} kWh, timestamp={}",
                    deviceId, String.format("%.3f", consumption), message.getTimestamp());

        } catch (Exception e) {
            log.error("Error generating/sending measurement: {}", e.getMessage(), e);
        }
    }


    @EventListener(ApplicationReadyEvent.class)
    public void seedFullDayOnStartup() {
        if (!seedFullDay) {
            return;
        }

        LocalDate today = LocalDate.now();
        log.info("Seeding full day of measurements for date={} and deviceId={}", today, deviceId);

        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime base = today.atTime(hour, 0);

            for (int i = 0; i < 6; i++) {
                LocalDateTime dt = base.plusMinutes(i * 10);
                Instant ts = dt.atZone(ZoneId.systemDefault()).toInstant();

                double consumption = generateRealisticConsumptionForHour(hour);

                MeasurementMessage msg = new MeasurementMessage(
                        UUID.fromString(deviceId),
                        consumption,
                        ts
                );
                rabbitTemplate.convertAndSend(queueName, msg);
            }
        }

        log.info("Finished seeding full day of measurements.");
    }

    private double generateRealisticConsumptionForHour(int hour) {
        double hourlyMultiplier;

        if (hour >= 0 && hour < 6) {
            hourlyMultiplier = 0.3 + random.nextDouble() * 0.2;
        } else if (hour >= 6 && hour < 9) {
            hourlyMultiplier = 0.6 + random.nextDouble() * 0.5;
        } else if (hour >= 9 && hour < 17) {
            hourlyMultiplier = 0.8 + random.nextDouble() * 0.6;
        } else if (hour >= 17 && hour < 22) {
            hourlyMultiplier = 1.5 + random.nextDouble() * 0.8;
        } else {
            hourlyMultiplier = 0.5 + random.nextDouble() * 0.4;
        }

        double tenMinuteConsumption = baseLoad * hourlyMultiplier / 6.0;
        double fluctuation = 1.0 + (random.nextDouble() - 0.5) * 0.2;

        return tenMinuteConsumption * fluctuation;
    }

    public void sendTestMeasurement(double value) {
        MeasurementMessage message = new MeasurementMessage(
                UUID.fromString(deviceId),
                value,
                Instant.now()
        );
        rabbitTemplate.convertAndSend(queueName, message);
        log.info("Sent TEST measurement: deviceId={}, value={}", deviceId, value);
    }
}
