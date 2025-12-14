package com.a1.demo.repositories;

import com.a1.demo.entities.DeviceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceAssignmentRepository extends JpaRepository<DeviceAssignment, UUID> {
    Optional<DeviceAssignment> findByDeviceId(UUID deviceId);
    List<DeviceAssignment> findAllByUserId(UUID userId);
    boolean existsByDeviceId(UUID deviceId);
    void deleteByDeviceId(UUID deviceId);
}