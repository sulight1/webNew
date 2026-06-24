package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.SensitiveWord;
import com.example.fingerartbackend.mapper.SensitiveWordMapper;
import com.example.fingerartbackend.service.SensitiveWordService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 敏感词服务实现类。
 */
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private static final List<String> DEFAULT_WORDS = Arrays.asList(
            "赌博", "色情", "假货", "诈骗", "传销", "刷单", "代考", "枪支", "毒品", "反动"
    );

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    /**
     * 执行 initDefaults 相关逻辑。
     */
    @PostConstruct
    @Override
    public void initDefaults() {
        if (sensitiveWordMapper.count() > 0) return;
        for (String w : DEFAULT_WORDS) {
            SensitiveWord word = new SensitiveWord();
            word.setWord(w);
            word.setEnabled(true);
            sensitiveWordMapper.save(word);
        }
    }

    /**
     * 校验数据。
     */
    @Override
    public void validateText(String text, String fieldLabel) {
        if (text == null || text.isBlank()) return;
        String lower = text.toLowerCase();
        for (SensitiveWord w : sensitiveWordMapper.findByEnabledTrue()) {
            if (lower.contains(w.getWord().toLowerCase())) {
                throw new RuntimeException(fieldLabel + "包含敏感词「" + w.getWord() + "」，请修改后重试");
            }
        }
    }

    /**
     * 查询敏感词列表。
     */
    @Override
    public List<Map<String, Object>> listWordDetails() {
        return sensitiveWordMapper.findByEnabledTrue().stream()
                .map(w -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", w.getId());
                    m.put("word", w.getWord());
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询敏感词列表。
     */
    @Override
    public List<String> listWords() {
        return sensitiveWordMapper.findByEnabledTrue().stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());
    }

    /**
     * 新增敏感词。
     */
    @Override
    public void addWord(String word) {
        if (word == null || word.isBlank()) return;
        if (sensitiveWordMapper.existsByWord(word.trim())) return;
        SensitiveWord entity = new SensitiveWord();
        entity.setWord(word.trim());
        sensitiveWordMapper.save(entity);
    }

    /**
     * 移除敏感词。
     */
    @Override
    public void removeWord(Long id) {
        sensitiveWordMapper.deleteById(id);
    }
}
