package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.CustomRequest;
import lombok.Data;

import java.util.List;

@Data
public class CustomRequestPageResult {
    private List<CustomRequest> items;
    private long total;
    private int page;
    private int size;
}
