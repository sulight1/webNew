package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.dto.DemandMatchResult;
import com.example.fingerartbackend.entity.CustomRequest;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.entity.ScheduleSlot;
import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.CustomRequestMapper;
import com.example.fingerartbackend.mapper.ProductMapper;
import com.example.fingerartbackend.mapper.ScheduleSlotMapper;
import com.example.fingerartbackend.mapper.SkillMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.DemandMatchService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.util.CategoryNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DemandMatchServiceImpl implements DemandMatchService {

    @Autowired
    private CustomRequestMapper requestMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private SkillMapper skillMapper;
    @Autowired
    private ScheduleSlotMapper scheduleSlotMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    public List<DemandMatchResult> matchArtisansForRequest(Long requestId, int limit) {
        CustomRequest request = requestMapper.findById(requestId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        return scoreArtisans(request).stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<DemandMatchResult> matchRequestsForArtisan(Long artisanId, int limit) {
        User artisan = userMapper.findById(artisanId).orElseThrow(() -> new RuntimeException("手作人不存在"));
        List<CustomRequest> openRequests = requestMapper.findAll().stream()
                .filter(r -> "OPEN".equals(r.getStatus()))
                .collect(Collectors.toList());

        List<DemandMatchResult> results = new ArrayList<>();
        for (CustomRequest request : openRequests) {
            DemandMatchResult match = scoreArtisanForRequest(artisan, request);
            if (match.getScore() >= 40) {
                match.setRequestId(request.getId());
                match.setRequestTitle(request.getTitle());
                match.setRequestCategory(request.getCategory());
                match.setBudgetMin(request.getBudgetMin());
                match.setBudgetMax(request.getBudgetMax());
                match.setDeadline(request.getDeadline());
                results.add(match);
            }
        }
        results.sort(Comparator.comparingInt(DemandMatchResult::getScore).reversed());
        return results.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public void notifyMatchedArtisans(CustomRequest request) {
        List<DemandMatchResult> matches = scoreArtisans(request).stream()
                .filter(m -> m.getScore() >= 50)
                .limit(5)
                .collect(Collectors.toList());
        for (DemandMatchResult match : matches) {
            notificationService.notify(
                    match.getArtisanId(),
                    "MATCH_REQUEST",
                    "有新的定制需求可接单",
                    "「" + request.getTitle() + "」与你的技能匹配，预算 ￥" + request.getBudgetMin() + "-" + request.getBudgetMax(),
                    "/custom-request-pool?requestId=" + request.getId()
            );
        }
    }

    private List<DemandMatchResult> scoreArtisans(CustomRequest request) {
        Map<Long, DemandMatchResult> map = new HashMap<>();
        List<User> users = userMapper.findAll().stream()
                .filter(u -> !"ADMIN".equals(u.getRole()))
                .filter(u -> request.getBuyer() == null || !u.getId().equals(request.getBuyer().getId()))
                .collect(Collectors.toList());

        for (User user : users) {
            DemandMatchResult result = scoreArtisanForRequest(user, request);
            if (result.getScore() > 0) {
                map.put(user.getId(), result);
            }
        }
        return map.values().stream()
                .sorted(Comparator.comparingInt(DemandMatchResult::getScore).reversed())
                .collect(Collectors.toList());
    }

    private DemandMatchResult scoreArtisanForRequest(User artisan, CustomRequest request) {
        DemandMatchResult result = new DemandMatchResult();
        result.setArtisanId(artisan.getId());
        result.setUsername(artisan.getUsername());
        result.setAvatar(artisan.getAvatar());
        result.setBio(artisan.getBio());
        result.setRating(artisan.getRating());
        result.setCreditScore(artisan.getCreditScore());
        result.setCompletedOrders(artisan.getCompletedOrders());

        int score = 0;
        List<String> reasons = new ArrayList<>();
        String reqCategory = CategoryNormalizer.normalize(request.getCategory());

        List<Product> products = productMapper.findByCreatorId(artisan.getId()).stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .collect(Collectors.toList());
        List<Skill> skills = skillMapper.findByUserId(artisan.getId()).stream()
                .filter(s -> "APPROVED".equals(s.getStatus()))
                .collect(Collectors.toList());

        boolean categoryHit = products.stream().anyMatch(p -> CategoryNormalizer.matches(p.getCategory(), reqCategory))
                || skills.stream().anyMatch(s -> CategoryNormalizer.matches(s.getCategory(), request.getCategory()));
        if (categoryHit) {
            score += 40;
            reasons.add("工艺分类匹配");
        }

        double avgPrice = products.stream()
                .map(Product::getPrice)
                .filter(p -> p != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(skills.stream().map(Skill::getZaowuBiCost).filter(c -> c != null).mapToInt(Integer::intValue).average().orElse(0));
        if (avgPrice > 0 && request.getBudgetMax() != null && request.getBudgetMin() != null) {
            if (avgPrice >= request.getBudgetMin() && avgPrice <= request.getBudgetMax()) {
                score += 25;
                reasons.add("报价区间契合");
            } else if (avgPrice <= request.getBudgetMax() * 1.2) {
                score += 10;
                reasons.add("报价接近预算");
            }
        }

        if (hasFreeSlotBeforeDeadline(artisan.getId(), request.getDeadline())) {
            score += 15;
            reasons.add("工期前排期空闲");
        }

        int credit = artisan.getCreditScore() != null ? artisan.getCreditScore() : 100;
        if (credit >= 100) {
            score += 10;
            reasons.add("信用良好");
        }
        double rating = artisan.getRating() != null ? artisan.getRating() : 5.0;
        if (rating >= 4.5) {
            score += 10;
            reasons.add("口碑优秀");
        }

        if (!products.isEmpty() || !skills.isEmpty()) {
            score += 5;
        }

        result.setScore(score);
        result.setReasons(reasons);
        return result;
    }

    private boolean hasFreeSlotBeforeDeadline(Long userId, String deadline) {
        if (deadline == null || deadline.isBlank()) return true;
        LocalDate target;
        try {
            target = LocalDate.parse(deadline, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return true;
        }
        LocalDate today = LocalDate.now();
        if (target.isBefore(today)) return false;
        List<ScheduleSlot> slots = scheduleSlotMapper.findByUserId(userId);
        return slots.stream()
                .anyMatch(s -> !s.getDate().isAfter(target)
                        && !s.getDate().isBefore(today)
                        && ("FREE".equals(s.getStatus()) || s.getStatus() == null));
    }
}
