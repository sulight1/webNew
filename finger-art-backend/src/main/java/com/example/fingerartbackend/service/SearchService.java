package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.SearchResultItem;
import com.example.fingerartbackend.mapper.CustomRequestMapper;
import com.example.fingerartbackend.mapper.ProductMapper;
import com.example.fingerartbackend.mapper.SkillMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class SearchService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CustomRequestMapper customRequestMapper;
    @Autowired
    private SkillMapper skillMapper;

    public List<SearchResultItem> search(String q, int limit) {
        if (q == null || q.trim().isEmpty()) {
            return List.of();
        }
        String kw = q.trim().toLowerCase(Locale.ROOT);
        int cap = limit > 0 ? limit : 30;
        List<SearchResultItem> items = new ArrayList<>();

        productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(p -> textMatch(kw, p.getTitle(), p.getDescription(), p.getCategory(), p.getCraftTechnique(), p.getCreator()))
                .limit(cap)
                .forEach(p -> items.add(SearchResultItem.product(
                        p.getId(), p.getTitle(),
                        joinSubtitle(p.getCategory(), p.getCreator()),
                        p.getImage(), p.getPrice())));

        customRequestMapper.findAll().stream()
                .filter(r -> "OPEN".equals(r.getStatus()))
                .filter(r -> textMatch(kw, r.getTitle(), r.getDescription(), r.getCategory()))
                .limit(Math.max(0, cap - items.size()))
                .forEach(r -> items.add(SearchResultItem.customRequest(
                        r.getId(), r.getTitle(),
                        joinSubtitle(r.getCategory(), r.getBudgetMin() != null ? "预算 ￥" + r.getBudgetMin() : ""))));

        skillMapper.findAll().stream()
                .filter(s -> "APPROVED".equals(s.getStatus()))
                .filter(s -> textMatch(kw, s.getTitle(), s.getDescription(), s.getCategory(), s.getUsername()))
                .limit(Math.max(0, cap - items.size()))
                .forEach(s -> items.add(SearchResultItem.skill(
                        s.getId(), s.getTitle(),
                        joinSubtitle(s.getCategory(), s.getUsername()))));

        return items.size() > cap ? items.subList(0, cap) : items;
    }

    private boolean textMatch(String kw, String... fields) {
        for (String field : fields) {
            if (field != null && field.toLowerCase(Locale.ROOT).contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private String joinSubtitle(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isEmpty()) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(part);
            }
        }
        return sb.toString();
    }
}
