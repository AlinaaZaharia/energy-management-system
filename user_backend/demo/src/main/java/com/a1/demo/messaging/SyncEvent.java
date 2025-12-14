package com.a1.demo.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncEvent {

    public enum EntityType { USER, DEVICE }
    public enum ActionType { CREATED, UPDATED, DELETED, ASSIGNED, UNASSIGNED }

    private EntityType entityType;
    private ActionType actionType;

    private UUID entityId;

    private String username;
    private String email;

    private String deviceName;
    private UUID userId;
    private Instant timestamp;

    public SyncEvent(EntityType entityType,
                     ActionType actionType,
                     UUID entityId,
                     String username,
                     String email) {
        this.entityType = entityType;
        this.actionType = actionType;
        this.entityId = entityId;
        this.username = username;
        this.email = email;
        this.timestamp = Instant.now();
    }

    public SyncEvent(EntityType entityType,
                     ActionType actionType,
                     UUID entityId,
                     String deviceName,
                     UUID userId) {
        this.entityType = entityType;
        this.actionType = actionType;
        this.entityId = entityId;
        this.deviceName = deviceName;
        this.userId = userId;
        this.timestamp = Instant.now();
    }
}
