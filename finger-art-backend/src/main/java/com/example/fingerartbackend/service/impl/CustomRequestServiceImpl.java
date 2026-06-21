package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.dto.CustomRequestPageResult;
import com.example.fingerartbackend.entity.CustomRequest;
import com.example.fingerartbackend.mapper.CustomRequestMapper;
import com.example.fingerartbackend.service.CustomRequestService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomRequestServiceImpl implements CustomRequestService {

    @Autowired
    private CustomRequestMapper requestMapper;

    @Override
    public CustomRequestPageResult search(
            String status,
            String category,
            String keyword,
            String sort,
            int page,
            int size) {
        Specification<CustomRequest> spec = buildSpec(status, category, keyword);
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.min(Math.max(size, 1), 50),
                resolveSort(sort));
        Page<CustomRequest> result = requestMapper.findAll(spec, pageable);

        CustomRequestPageResult body = new CustomRequestPageResult();
        body.setItems(result.getContent());
        body.setTotal(result.getTotalElements());
        body.setPage(Math.max(page, 1));
        body.setSize(pageable.getPageSize());
        return body;
    }

    private Specification<CustomRequest> buildSpec(String status, String category, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category.trim())) {
                predicates.add(cb.equal(root.get("category"), category.trim()));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort resolveSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createTime");
        }
        return switch (sort) {
            case "budget" -> Sort.by(Sort.Direction.DESC, "budgetMax");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "deadline");
            default -> Sort.by(Sort.Direction.DESC, "createTime");
        };
    }
}
