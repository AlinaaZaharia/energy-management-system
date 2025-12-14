package a2.monitoring_service.repository;

import a2.monitoring_service.model.SyncedDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SyncedDeviceRepository extends JpaRepository<SyncedDevice, UUID> {}
