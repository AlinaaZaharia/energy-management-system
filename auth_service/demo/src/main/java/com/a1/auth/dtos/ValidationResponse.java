package com.a1.auth.dtos;

import com.a1.auth.entity.Role;
import java.util.UUID;

public class ValidationResponse {
    private boolean valid;
    private UUID userId;
    private String username;
    private Role role;
    private String error;

    public ValidationResponse() {}

    public ValidationResponse(boolean valid) {
        this.valid = valid;
    }

    public ValidationResponse(boolean valid, UUID userId, String username, Role role) {
        this.valid = valid;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}