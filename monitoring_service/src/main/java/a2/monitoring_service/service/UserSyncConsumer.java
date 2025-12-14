package a2.monitoring_service.service;

import a2.monitoring_service.messaging.SyncEvent;
import a2.monitoring_service.model.SyncedUser;
import a2.monitoring_service.repository.SyncedUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserSyncConsumer.class);

    private final SyncedUserRepository syncedUserRepository;

    public UserSyncConsumer(SyncedUserRepository syncedUserRepository) {
        this.syncedUserRepository = syncedUserRepository;
    }

    @RabbitListener(queues = "${sync.queue.name}")
    public void handleUserSync(SyncEvent event) {
        if (event.getEntityType() != SyncEvent.EntityType.USER) {
            return;
        }

        UUID userId = event.getEntityId();
        log.info("Received USER sync event: action={}, userId={}, username={}, email={}",
                event.getActionType(), userId, event.getUsername(), event.getEmail());

        switch (event.getActionType()) {
            case CREATED, UPDATED -> handleUpsertUser(event);
            case DELETED -> handleDeleteUser(event);
        }
    }

    private void handleUpsertUser(SyncEvent event) {
        UUID userId = event.getEntityId();

        SyncedUser user = syncedUserRepository
                .findById(userId)
                .orElseGet(() -> {
                    SyncedUser u = new SyncedUser();
                    u.setId(userId);
                    return u;
                });

        user.setUsername(event.getUsername());
        user.setEmail(event.getEmail());
        user.setDeleted(false);
        user.setLastSyncTime(Instant.now());

        syncedUserRepository.save(user);

        log.info("Synced USER {} with id={}, username={}, email={}",
                event.getActionType(), userId, user.getUsername(), user.getEmail());
    }

    private void handleDeleteUser(SyncEvent event) {
        UUID userId = event.getEntityId();

        Optional<SyncedUser> opt = syncedUserRepository.findById(userId);
        if (opt.isEmpty()) {
            log.warn("Received USER_DELETED sync for unknown userId={}", userId);
            return;
        }

        SyncedUser user = opt.get();
        user.setDeleted(true);
        user.setLastSyncTime(Instant.now());
        syncedUserRepository.save(user);

        log.info("Marked USER id={} as deleted in synced_users", userId);
    }
}
