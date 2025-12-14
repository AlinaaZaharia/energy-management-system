package com.a1.demo.controllers;

import com.a1.demo.dtos.UserDTO;
import com.a1.demo.dtos.UserDetailsDTO;
import com.a1.demo.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/users")
@Validated
@Tag(name = "User Management", description = "CRUD operations for user accounts")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
            summary = "Get all users (simplified)",
            description = "Retrieve a simplified list of all users containing only ID, username, age, and email"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of users",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class)))
    public ResponseEntity<List<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.findUsers());
    }

    @GetMapping("/details")
    @Operation(
            summary = "Get all users with full details",
            description = "Retrieve a comprehensive list of all users including ID, username, email, name, city, and date of birth")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved detailed list of users",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDetailsDTO.class)))
    public ResponseEntity<List<UserDetailsDTO>> getUsersWithDetails(){
        return ResponseEntity.ok(userService.findAllWithDetails());
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully, Location header contains user URI"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")})
    public ResponseEntity<Void> create(@Valid @RequestBody UserDetailsDTO user) {
        UUID id = userService.insert(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve detailed information about a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<UserDetailsDTO> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")})
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @Valid @RequestBody UserDetailsDTO dto){
        userService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hello")
    @Operation(summary = "Health check", description = "Simple health check endpoint")
    @ApiResponse(responseCode = "200", description = "Service is running")
    public String hello(){
        return "Hello from USER Service!";
    }
}