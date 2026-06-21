package com.example.fingerartbackend.auth;

public record AuthUser(Long id, String username, String role, boolean adminSession) {
}
