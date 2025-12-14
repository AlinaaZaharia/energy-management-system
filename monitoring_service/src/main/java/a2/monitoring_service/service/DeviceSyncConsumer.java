package a2.monitoring_service.service;

import a2.monitoring_service.messaging.SyncEvent;
import a2.monitoring_service.model.SyncedDevice;
import a2.monitoring_service.repository.SyncedDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DeviceSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeviceSyncConsumer.class);

    private final SyncedDeviceRepository repo;

    public DeviceSyncConsumer(SyncedDeviceRepository repo) {
        this.repo = repo;
    }

    @RabbitListener(queues = "${sync.queue.name}")
    public void onDeviceEvent(SyncEvent event) {

        if (event.getEntityType() != SyncEvent.EntityType.DEVICE) return;

        switch (event.getActionType()) {
            case CREATED, UPDATED, ASSIGNED, UNASSIGNED -> handleUpsert(event);
            case DELETED -> handleDelete(event);
        }
    }

    private void handleUpsert(SyncEvent event) {

        SyncedDevice d = repo.findById(event.getEntityId())
                .orElseGet(SyncedDevice::new);

        d.setId(event.getEntityId());
        d.setName(event.getDeviceName());
        d.setUserId(event.getUserId());

        if (event.getMaxConsumption() != null) {
            d.setMaxConsumption(event.getMaxConsumption());
        }

        d.setDeleted(false);
        d.setLastSyncTime(Instant.now());

        repo.save(d);

        log.info("Device synced: id={}, name={}, userId={}, maxConsumption={}",
                d.getId(), d.getName(), d.getUserId(), d.getMaxConsumption());
    }

    private void handleDelete(SyncEvent event) {

        repo.findById(event.getEntityId()).ifPresent(d -> {
            d.setDeleted(true);
            d.setLastSyncTime(Instant.now());
            repo.save(d);
        });

        log.info("Device marked as deleted: {}", event.getEntityId());
    }
}
