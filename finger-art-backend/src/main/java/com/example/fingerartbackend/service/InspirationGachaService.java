package com.example.fingerartbackend.service;

import java.util.Map;

public interface InspirationGachaService {

    Map<String, Object> getStatus(Long userId);

    Map<String, Object> draw(Long userId, boolean useFree);

    Map<String, Object> generateImage(Long userId, String imagePrompt);
}
