package a2.monitoring_service.repository;

import a2.monitoring_service.model.SyncedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SyncedUserRepository extends JpaRepository<SyncedUser, UUID> {
}
