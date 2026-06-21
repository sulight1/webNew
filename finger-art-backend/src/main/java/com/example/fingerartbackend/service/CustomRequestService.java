package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.CustomRequestPageResult;

public interface CustomRequestService {

    CustomRequestPageResult search(
            String status,
            String category,
            String keyword,
            String sort,
            int page,
            int size);
}
