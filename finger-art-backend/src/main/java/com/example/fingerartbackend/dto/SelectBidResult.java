package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.CustomRequest;
import lombok.Data;

@Data
public class SelectBidResult {
    private CustomOrder order;
    private CustomRequest request;
}
