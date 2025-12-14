package a2.monitoring_service.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementMessage {
    private UUID deviceId;
    private Double measurementValue;
    private Instant timestamp;
}