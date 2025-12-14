package com.a1.demo.dtos;

import java.util.UUID;

public class DeviceAssignmentDTO {
    private UUID id;
    private UUID deviceId;
    private UUID userId;
    private String deviceName;
    private String userName;

    public DeviceAssignmentDTO() {}

    public DeviceAssignmentDTO(UUID id, UUID deviceId, UUID userId) {
        this.id = id;
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}