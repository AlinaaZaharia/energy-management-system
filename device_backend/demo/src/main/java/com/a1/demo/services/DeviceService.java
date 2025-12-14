package com.a1.demo.services;

import com.a1.demo.dtos.DeviceDTO;
import com.a1.demo.dtos.DeviceDetailsDTO;
import com.a1.demo.dtos.builders.DeviceBuilder;
import com.a1.demo.entities.Device;
import com.a1.demo.entities.DeviceAssignment;
import com.a1.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.a1.demo.messaging.SyncEvent;
import com.a1.demo.messaging.SyncEventPublisher;
import com.a1.demo.repositories.DeviceAssignmentRepository;
import com.a1.demo.repositories.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final SyncEventPublisher syncEventPublisher;

    public DeviceService(DeviceRepository deviceRepository,
                         DeviceAssignmentRepository assignmentRepository,
                         SyncEventPublisher syncEventPublisher) {
        this.deviceRepository = deviceRepository;
        this.assignmentRepository = assignmentRepository;
        this.syncEventPublisher = syncEventPublisher;
    }

    public List<DeviceDTO> findDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceDetailsDTO> findDevicesWithDetails() {
        return deviceRepository.findAll()
                .stream()
                .map(device -> {
                    UUID userId = assignmentRepository.findByDeviceId(device.getId())
                            .map(DeviceAssignment::getUserId)
                            .orElse(null);

                    return DeviceBuilder.toDeviceDetailsDTO(device, userId);
                })
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id: " + id));
        UUID userId = assignmentRepository.findByDeviceId(id)
                .map(DeviceAssignment::getUserId)
                .orElse(null);

        return DeviceBuilder.toDeviceDetailsDTO(device, userId);
    }

    @Transactional
    public UUID insert(DeviceDetailsDTO dto) {
        if (deviceRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Device name already exists");
        }

        Device device = deviceRepository.save(DeviceBuilder.toEntity(dto));

        if (dto.getUserId() != null) {
            DeviceAssignment assignment = new DeviceAssignment();
            assignment.setDeviceId(device.getId());
            assignment.setUserId(dto.getUserId());
            assignmentRepository.save(assignment);
        }

        syncEventPublisher.publish(new SyncEvent(
                SyncEvent.EntityType.DEVICE,
                SyncEvent.ActionType.CREATED,
                device.getId(),
                null,
                null,
                device.getName(),
                dto.getUserId(),
                device.getMaxConsumption().doubleValue(),
                Instant.now()
        ));


        return device.getId();
    }

    @Transactional
    public void update(UUID id, DeviceDetailsDTO dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id: " + id));

        if (!device.getName().equalsIgnoreCase(dto.getName())
                && deviceRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Device name already exists");
        }

        device.setName(dto.getName());
        device.setMaxConsumption(dto.getMaxConsumption());
        device.setDescription(dto.getDescription());
        deviceRepository.save(device);

        if (dto.getUserId() != null) {
            assignToUser(id, dto.getUserId());
        }

        syncEventPublisher.publish(new SyncEvent(
                SyncEvent.EntityType.DEVICE,
                SyncEvent.ActionType.UPDATED,
                device.getId(),
                null,
                null,
                device.getName(),
                dto.getUserId(),
                device.getMaxConsumption().doubleValue(),
                Instant.now()
        ));

    }

    @Transactional
    public void delete(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id: " + id));

        assignmentRepository.deleteByDeviceId(id);
        deviceRepository.deleteById(id);

        syncEventPublisher.publish(new SyncEvent(
                SyncEvent.EntityType.DEVICE,
                SyncEvent.ActionType.DELETED,
                device.getId(),
                null,
                null,
                device.getName(),
                null,
                device.getMaxConsumption().doubleValue(),
                Instant.now()
        ));

    }

    @Transactional
    public void assignToUser(UUID deviceId, UUID userId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id: " + deviceId));

        var existingAssignment = assignmentRepository.findByDeviceId(deviceId);

        if (existingAssignment.isPresent()) {
            DeviceAssignment assignment = existingAssignment.get();
            assignment.setUserId(userId);
            assignmentRepository.save(assignment);
        } else {
            DeviceAssignment assignment = new DeviceAssignment();
            assignment.setDeviceId(deviceId);
            assignment.setUserId(userId);
            assignmentRepository.save(assignment);
        }

        syncEventPublisher.publish(new SyncEvent(
                SyncEvent.EntityType.DEVICE,
                SyncEvent.ActionType.ASSIGNED,
                deviceId,
                null,
                null,
                device.getName(),
                userId,
                null,
                Instant.now()
        ));

    }

    @Transactional
    public void unassignFromUser(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id: " + deviceId));

        assignmentRepository.deleteByDeviceId(deviceId);

        syncEventPublisher.publish(new SyncEvent(
                SyncEvent.EntityType.DEVICE,
                SyncEvent.ActionType.UNASSIGNED,
                deviceId,
                null,
                null,
                device.getName(),
                null,
                null,
                Instant.now()
        ));

    }

    public List<DeviceDetailsDTO> findDevicesByUserId(UUID userId) {
        List<UUID> deviceIds = assignmentRepository.findAllByUserId(userId)
                .stream()
                .map(DeviceAssignment::getDeviceId)
                .collect(Collectors.toList());

        return deviceRepository.findAllById(deviceIds)
                .stream()
                .map(device -> DeviceBuilder.toDeviceDetailsDTO(device, userId))
                .collect(Collectors.toList());
    }
}