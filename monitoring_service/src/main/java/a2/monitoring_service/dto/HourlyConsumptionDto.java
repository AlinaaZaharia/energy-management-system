package a2.monitoring_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyConsumptionDto {
    private int hour;
    private Double consumption;
}