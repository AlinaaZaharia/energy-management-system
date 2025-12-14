package com.a1.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class DeviceDetailsDTO {
    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull
    @Positive(message = "maxConsumption must be > 0")
    private BigDecimal maxConsumption;

    private String description;

    private UUID userId;

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(UUID id, String name, BigDecimal maxConsumption, String description, UUID userId) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.description = description;
        this.userId = userId;
    }

    public DeviceDetailsDTO(String name, BigDecimal maxConsumption, String description) {
        this(null, name, maxConsumption, description, null);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDetailsDTO that = (DeviceDetailsDTO) o;
        return Objects.equals(name, that.name)
                && Objects.equals(maxConsumption, that.maxConsumption)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, maxConsumption, description);
    }
}