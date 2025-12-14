package com.a1.auth.controller;

import com.a1.auth.dtos.*;
import com.a1.auth.service.AuthService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and credential management endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the authentication service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user with credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(example = "{\"message\":\"User registered successfully\"}"))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(schema = @Schema(example = "{\"error\":\"Username already exists\"}"))),
            @ApiResponse(responseCode = "500", description = "Registration failed")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Login failed")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed"));
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Validate a JWT token and return user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<ValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ValidationResponse response = new ValidationResponse(false);
                response.setError("No token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);
            ValidationResponse response = authService.validateToken(token);

            if (response.isValid()) {
                return ResponseEntity.ok(response);
            } else {
                response.setError("Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            ValidationResponse response = new ValidationResponse(false);
            response.setError("Validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/credentials")
    @Operation(summary = "Create credential", description = "Create authentication credential for existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Credential created successfully"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @ApiResponse(responseCode = "500", description = "Failed to create credential")
    })
    public ResponseEntity<?> createCredential(@Valid @RequestBody CreateCredentialRequest request) {
        try {
            authService.createCredential(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Credential created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create credential"));
        }
    }

    @PutMapping("/credentials/{userId}")
    @Operation(summary = "Update credential", description = "Update username, email, password, and role for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential updated successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to update credential")
    })
    public ResponseEntity<?> updateCredential(@PathVariable UUID userId,
                                              @Valid @RequestBody UpdateCredentialRequest request) {
        try {
            authService.updateCredential(userId, request);
            return ResponseEntity.ok(Map.of("message", "Credential updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update credential"));
        }
    }

    @GetMapping("/credentials/role")
    @Operation(summary = "Get user role", description = "Get the role of a user by their userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role retrieved successfully",
                    content = @Content(schema = @Schema(example = "{\"role\":\"ADMIN\"}"))),
            @ApiResponse(responseCode = "404", description = "Credential not found")
    })
    public ResponseEntity<?> getRoleByUserId(@RequestParam UUID userId) {
        try {
            String role = authService.getRoleByUserId(userId);
            return ResponseEntity.ok(Map.of("role", role));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Credential not found"));
        }
    }

    @PutMapping("/credentials/role")
    @Operation(summary = "Update user role", description = "Update the role of an existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to update role")
    })
    public ResponseEntity<?> updateRole(@Valid @RequestBody UpdateRoleRequest request) {
        try {
            authService.updateRole(request.getUserId(), request.getRole());
            return ResponseEntity.ok(Map.of("message", "Role updated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update role"));
        }
    }
}