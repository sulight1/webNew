package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private User user;
}
