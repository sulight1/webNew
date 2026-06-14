package com.example.fingerartbackend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    /** 数字账号，唯一 */
    private String account;
    private String password;
    private String confirmPassword;
}
