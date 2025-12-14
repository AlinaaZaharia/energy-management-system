package a2.monitoring_service.repository;

import a2.monitoring_service.model.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, Long> {

    Optional<HourlyConsumption> findByDeviceIdAndHourTimestamp(
            UUID deviceId,
            LocalDateTime hourTimestamp
    );

    List<HourlyConsumption> findByDeviceIdAndHourTimestampBetween(
            UUID deviceId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<HourlyConsumption> findByDeviceIdAndHourTimestampBetweenOrderByHourTimestamp(
            UUID deviceId,
            Instant start,
            Instant end
    );
}