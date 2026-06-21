package com.example.fingerartbackend.dto;

import lombok.Data;

@Data
public class TotpSetupResponse {
    private String secret;
    private String otpAuthUrl;
    private String qrCodeDataUri;
}
