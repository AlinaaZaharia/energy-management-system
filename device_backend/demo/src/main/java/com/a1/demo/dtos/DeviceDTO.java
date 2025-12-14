package com.a1.demo.dtos;

import java.math.BigDecimal;
import java.util.UUID;


public class DeviceDTO {
    private UUID id;
    private String name;
    private BigDecimal maxConsumption;

    public DeviceDTO() {}
    public DeviceDTO(UUID id, String name, BigDecimal maxConsumption) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(BigDecimal maxConsumption) {
        this.maxConsumption = maxConsumption;
    }
}