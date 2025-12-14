package com.a1.auth.repository;

import com.a1.auth.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByUsername(String username);
    Optional<Credential> findByUserId(UUID userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}