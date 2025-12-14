package com.a1.demo.dtos.builders;

import com.a1.demo.dtos.DeviceDTO;
import com.a1.demo.dtos.DeviceDetailsDTO;
import com.a1.demo.entities.Device;

import java.util.UUID;

public class DeviceBuilder {

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption()
        );
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getDescription(),
                null
        );
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device, UUID userId) {
        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getDescription(),
                userId
        );
    }

    public static Device toEntity(DeviceDetailsDTO dto) {
        return new Device(
                dto.getName(),
                dto.getMaxConsumption(),
                dto.getDescription()
        );
    }
}