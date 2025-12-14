package com.a1.auth.dtos;

import com.a1.auth.entity.Role;
import java.util.UUID;

public class LoginResponse {
    private String token;
    private UUID userId;
    private String username;
    private Role role;

    public LoginResponse() {}

    public LoginResponse(String token, UUID userId, String username, Role role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}