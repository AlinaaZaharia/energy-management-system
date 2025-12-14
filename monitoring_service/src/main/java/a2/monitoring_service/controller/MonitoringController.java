package a2.monitoring_service.controller;

import a2.monitoring_service.dto.HourlyConsumptionDto;
import a2.monitoring_service.model.HourlyConsumption;
import a2.monitoring_service.repository.HourlyConsumptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private static final Logger log = LoggerFactory.getLogger(MonitoringController.class);
    private final HourlyConsumptionRepository repository;

    public MonitoringController(HourlyConsumptionRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{deviceId}")
    public List<HourlyConsumptionDto> getDailyConsumption(
            @PathVariable UUID deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("Fetching consumption for deviceId={}, date={}", deviceId, date);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<HourlyConsumption> list = repository
                .findByDeviceIdAndHourTimestampBetween(deviceId, start, end);

        log.info("Found {} hourly consumption records", list.size());

        Map<Integer, Double> hourToConsumption = list.stream()
                .collect(Collectors.toMap(
                        h -> h.getHourTimestamp().getHour(),
                        HourlyConsumption::getTotalConsumption,
                        Double::sum
                ));

        return java.util.stream.IntStream.range(0, 24)
                .mapToObj(hour -> new HourlyConsumptionDto(
                        hour,
                        hourToConsumption.getOrDefault(hour, 0.0)
                ))
                .collect(Collectors.toList());
    }


    @GetMapping("/health")
    public String health() {
        return "Monitoring Service is running!";
    }
}