package com.example.fingerartbackend.dto;

import lombok.Data;

@Data
public class TotpEnableRequest {
    private String secret;
    private String code;
}
