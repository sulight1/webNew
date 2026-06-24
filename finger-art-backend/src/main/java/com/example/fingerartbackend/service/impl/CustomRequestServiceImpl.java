package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.dto.CustomRequestPageResult;
import com.example.fingerartbackend.entity.CustomRequest;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.CustomRequestMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.CustomRequestService;
import com.example.fingerartbackend.service.DemandMatchService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.SensitiveWordService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 定制需求服务实现类。
 */
@Service
public class CustomRequestServiceImpl implements CustomRequestService {

    @Autowired
    private CustomRequestMapper requestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private DemandMatchService demandMatchService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 搜索定制需求。
     */
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

    /**
     * 构建响应对象。
     */
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

    /**
     * 执行 resolveSort 相关逻辑。
     */
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

    /**
     * 创建定制需求。
     */
    @Override
    @Transactional
    public CustomRequest createRequest(Map<String, Object> payload) {
        CustomRequest request = new CustomRequest();
        String title = payload.get("title") != null ? payload.get("title").toString().trim() : "";
        String description = payload.get("description") != null ? payload.get("description").toString() : "";
        sensitiveWordService.validateText(title, "需求标题");
        sensitiveWordService.validateText(description, "需求描述");

        request.setTitle(title);
        request.setCategory((String) payload.get("category"));
        request.setDescription(description);
        request.setBudgetMin(Double.valueOf(payload.get("budgetMin").toString()));
        request.setBudgetMax(Double.valueOf(payload.get("budgetMax").toString()));
        request.setDeadline((String) payload.get("deadline"));
        if (payload.get("referenceImage") != null) {
            String ref = payload.get("referenceImage").toString().trim();
            if (!ref.isEmpty()) {
                request.setReferenceImage(ref);
            }
        }

        Long buyerId = Long.valueOf(payload.get("buyerId").toString());
        User buyer = userMapper.findById(buyerId).orElseThrow(() -> new RuntimeException("用户不存在"));
        request.setBuyer(buyer);
        request.setStatus("PENDING");
        return requestMapper.save(request);
    }

    /**
     * 审核定制需求。
     */
    @Override
    @Transactional
    public CustomRequest auditRequest(Long id, String status) {
        if (!"OPEN".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("无效的审核状态");
        }
        CustomRequest request = requestMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("该需求不在待审核状态");
        }
        request.setStatus(status);
        CustomRequest saved = requestMapper.save(request);
        if ("OPEN".equals(status)) {
            demandMatchService.notifyMatchedArtisans(saved);
        }
        notifyBuyerAuditResult(saved, status);
        return saved;
    }

    /**
     * 发送通知。
     */
    private void notifyBuyerAuditResult(CustomRequest request, String status) {
        if (request.getBuyer() == null) {
            return;
        }
        Long buyerId = request.getBuyer().getId();
        if ("OPEN".equals(status)) {
            notificationService.notify(
                    buyerId,
                    "REQUEST_AUDIT",
                    "定制需求已通过",
                    "你的需求「" + request.getTitle() + "」已审核通过，现已在需求大厅招募中",
                    "/account?menu=my-requests");
        } else if ("REJECTED".equals(status)) {
            notificationService.notify(
                    buyerId,
                    "REQUEST_AUDIT",
                    "定制需求未通过",
                    "你的需求「" + request.getTitle() + "」未通过审核，可修改后重新发布",
                    "/account?menu=my-requests");
        }
    }
}
