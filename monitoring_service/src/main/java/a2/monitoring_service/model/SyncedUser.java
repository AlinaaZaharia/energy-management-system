package a2.monitoring_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "synced_users")
@Getter
@Setter
@NoArgsConstructor
public class SyncedUser {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 30)
    private String username;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "last_sync_time", nullable = false)
    private Instant lastSyncTime;

    public SyncedUser(UUID id, String username, String email, boolean deleted, Instant lastSyncTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.deleted = deleted;
        this.lastSyncTime = lastSyncTime;
    }
}
