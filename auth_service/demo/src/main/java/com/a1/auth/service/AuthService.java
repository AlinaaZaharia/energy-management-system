package com.a1.auth.service;

import com.a1.auth.dtos.*;
import com.a1.auth.entity.Credential;
import com.a1.auth.entity.Role;
import com.a1.auth.repository.CredentialRepository;
import com.a1.auth.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final CredentialRepository credentialRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(CredentialRepository credentialRepository, JwtUtil jwtUtil) {
        this.credentialRepository = credentialRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void register(RegisterRequest request) {
        logger.info("🔹 Registering user: {}", request.getUsername());

        if (credentialRepository.existsByUsername(request.getUsername())) {
            logger.warn("Username already exists: {}", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        if (request.getEmail() != null && credentialRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        Credential credential = new Credential();
        credential.setUsername(request.getUsername());
        credential.setEmail(request.getEmail());

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        logger.debug("Hashed password (first 20 chars): {}", hashedPassword.substring(0, 20));

        credential.setPasswordHash(hashedPassword);
        credential.setRole(request.getRole());

        credentialRepository.save(credential);
        logger.info("User registered successfully: {}", request.getUsername());
    }

    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getUsername());

        Credential credential = credentialRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", request.getUsername());
                    return new IllegalArgumentException("Invalid credentials");});
        logger.debug("Found user: {} with role: {}", credential.getUsername(), credential.getRole());
        logger.debug("Stored password hash (first 20 chars): {}", credential.getPasswordHash().substring(0, 20));
        logger.debug("Attempting password match...");

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            logger.warn("Invalid password for user: {}", request.getUsername());
            throw new IllegalArgumentException("Invalid credentials");
        }
        logger.info("Password matched for user: {}", request.getUsername());

        try {
            String token = jwtUtil.generateToken(credential);
            logger.info("🎫 Token generated successfully for user: {}", request.getUsername());

            LoginResponse response = new LoginResponse(
                    token,
                    credential.getUserId() != null ? credential.getUserId() : credential.getId(),
                    credential.getUsername(),
                    credential.getRole()
            );
            logger.info("Login successful for user: {}", request.getUsername());
            return response;

        } catch (Exception e) {
            logger.error("Error generating token for user: {}", request.getUsername(), e);
            throw new RuntimeException("Error generating token", e);
        }
    }

    public ValidationResponse validateToken(String token) {
        logger.debug("🔹 Validating token...");
        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                var userId = jwtUtil.extractUserId(token);

                logger.debug("Token valid for user: {}", username);
                return new ValidationResponse(true, userId, username,
                        com.a1.auth.entity.Role.valueOf(role));
            }
            logger.warn("Token validation failed");
            return new ValidationResponse(false);
        } catch (Exception e) {
            logger.error("Token validation error", e);
            return new ValidationResponse(false);
        }
    }

    @Transactional
    public void createCredential(CreateCredentialRequest request) {
        if (credentialRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Credential credential = new Credential();
        credential.setUserId(request.getUserId());
        credential.setUsername(request.getUsername());
        credential.setEmail(request.getEmail());
        credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credential.setRole(Role.valueOf(request.getRole()));

        credentialRepository.save(credential);
    }

    @Transactional
    public void updateCredential(UUID userId, UpdateCredentialRequest request) {
        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found"));

        if (!credential.getUsername().equals(request.getUsername())
                && credentialRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (!credential.getEmail().equals(request.getEmail())
                && credentialRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        credential.setUsername(request.getUsername());
        credential.setEmail(request.getEmail());
        credential.setRole(Role.valueOf(request.getRole()));

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        credentialRepository.save(credential);
    }

    public String getRoleByUserId(UUID userId) {
        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found for userId: " + userId));
        return credential.getRole().toString();
    }

    @Transactional
    public void updateRole(UUID userId, String newRole) {
        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found"));
        credential.setRole(Role.valueOf(newRole));
        credentialRepository.save(credential);
    }
}