package com.example.fingerartbackend.dto;

import lombok.Data;

@Data
public class TotpVerifyRequest {
    private String preAuthToken;
    private String code;
}
