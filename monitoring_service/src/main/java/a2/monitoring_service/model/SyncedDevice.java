package a2.monitoring_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "synced_devices")
@Getter
@Setter
@NoArgsConstructor
public class SyncedDevice {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "max_consumption")
    private Double maxConsumption;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "last_sync_time", nullable = false)
    private Instant lastSyncTime;

    public SyncedDevice(UUID id,
                        String name,
                        UUID userId,
                        Double maxConsumption,
                        boolean deleted,
                        Instant lastSyncTime) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.maxConsumption = maxConsumption;
        this.deleted = deleted;
        this.lastSyncTime = lastSyncTime;
    }
}
