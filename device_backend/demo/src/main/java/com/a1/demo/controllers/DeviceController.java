package com.a1.demo.controllers;

import com.a1.demo.dtos.DeviceDetailsDTO;
import com.a1.demo.services.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@Validated
@Tag(name = "Device Management", description = "CRUD operations for IoT devices and user assignments")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "Get all devices", description = "Retrieve a list of all devices with assignment information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of devices")
    public ResponseEntity<List<DeviceDetailsDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevicesWithDetails());
    }

    @PostMapping
    @Operation(summary = "Create new device", description = "Register a new IoT device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")})
    public ResponseEntity<Void> create(@Valid @RequestBody DeviceDetailsDTO dto) {
        UUID id = deviceService.insert(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieve detailed information about a specific device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found")})
    public ResponseEntity<DeviceDetailsDTO> get(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update device", description = "Update device information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device updated successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found")})
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @Valid @RequestBody DeviceDetailsDTO dto) {
        deviceService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete device", description = "Remove a device from the system")
    @ApiResponse(responseCode = "204", description = "Device deleted successfully")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{deviceId}/assign/{userId}")
    @Operation(summary = "Assign device to user", description = "Create an assignment between a device and a user")
    @ApiResponse(responseCode = "204", description = "Device assigned successfully")
    public ResponseEntity<Void> assignToUser(@PathVariable UUID deviceId,
                                             @PathVariable UUID userId) {
        deviceService.assignToUser(deviceId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{deviceId}/assign")
    @Operation(summary = "Unassign device", description = "Remove device assignment from user")
    @ApiResponse(responseCode = "204", description = "Device unassigned successfully")
    public ResponseEntity<Void> unassignFromUser(@PathVariable UUID deviceId) {
        deviceService.unassignFromUser(deviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's devices", description = "Retrieve all devices assigned to a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user's devices")
    public ResponseEntity<List<DeviceDetailsDTO>> getDevicesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.findDevicesByUserId(userId));
    }

    @GetMapping("/hello")
    @Operation(summary = "Health check", description = "Simple health check endpoint")
    @ApiResponse(responseCode = "200", description = "Service is running")
    public String hello() {
        return "Hello from DEVICE Service!";
    }
}