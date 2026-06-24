package com.example.fingerartbackend.service;

import java.util.List;
import java.util.Map;

/**
 * 敏感词服务接口，定义业务能力（业务服务接口）。
 */
public interface SensitiveWordService {
    void validateText(String text, String fieldLabel);
    List<String> listWords();
    List<Map<String, Object>> listWordDetails();
    void addWord(String word);
    void removeWord(Long id);
    void initDefaults();
}
