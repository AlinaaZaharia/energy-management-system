package a2.monitoring_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hourly_consumption")
@Getter
@Setter
@NoArgsConstructor
public class HourlyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "hour_timestamp", nullable = false)
    private LocalDateTime hourTimestamp;

    @Column(name = "total_consumption", nullable = false)
    private Double totalConsumption = 0.0;

    public HourlyConsumption(UUID deviceId, LocalDateTime hourTimestamp, Double totalConsumption) {
        this.deviceId = deviceId;
        this.hourTimestamp = hourTimestamp;
        this.totalConsumption = totalConsumption;
    }

    public void addToTotal(Double value) {
        if (value == null) {
            return;
        }
        if (this.totalConsumption == null) {
            this.totalConsumption = 0.0;
        }
        this.totalConsumption += value;
    }
}