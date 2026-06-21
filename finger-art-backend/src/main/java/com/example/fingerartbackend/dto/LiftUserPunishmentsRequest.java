package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class LiftUserPunishmentsRequest {
    private List<String> types;
}
