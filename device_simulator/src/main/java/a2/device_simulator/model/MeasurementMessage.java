package a2.device_simulator.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant timestamp;

    @Override
    public String toString() {
        return "MeasurementMessage{" +
                "deviceId=" + deviceId +
                ", measurementValue=" + String.format("%.3f", measurementValue) + " kWh" +
                ", timestamp=" + timestamp +
                '}';
    }
}