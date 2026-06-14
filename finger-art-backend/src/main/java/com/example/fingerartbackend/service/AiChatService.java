package com.example.fingerartbackend.service;

import java.util.List;
import java.util.Map;

public interface AiChatService {

    Map<String, Object> chat(List<Map<String, String>> messages, Long userId, String pageContext);

    Map<String, Object> recommend(String query, int limit);
}
