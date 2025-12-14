package com.a1.demo.repositories;

import com.a1.demo.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
}