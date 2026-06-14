package com.example.fingerartbackend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    /** 数字账号 */
    private String account;
    private String password;
}
