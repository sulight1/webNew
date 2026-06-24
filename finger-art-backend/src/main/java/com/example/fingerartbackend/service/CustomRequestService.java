package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.CustomRequestPageResult;
import com.example.fingerartbackend.entity.CustomRequest;

import java.util.Map;

/**
 * 定制需求服务接口，定义业务能力（业务服务接口）。
 */
public interface CustomRequestService {

    CustomRequestPageResult search(
            String status,
            String category,
            String keyword,
            String sort,
            int page,
            int size);

    CustomRequest createRequest(Map<String, Object> payload);

    CustomRequest auditRequest(Long id, String status);
}
