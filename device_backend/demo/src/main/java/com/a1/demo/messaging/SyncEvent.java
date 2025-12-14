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

    public enum ActionType {
        CREATED,
        UPDATED,
        DELETED,
        ASSIGNED,
        UNASSIGNED
    }

    private EntityType entityType;
    private ActionType actionType;

    private UUID entityId;

    private String username;
    private String email;

    private String deviceName;
    private UUID userId;
    private Double maxConsumption;

    private Instant timestamp;
}
