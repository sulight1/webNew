package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.*;
import com.example.fingerartbackend.mapper.CustomOrderMapper;
import com.example.fingerartbackend.mapper.ProductMapper;
import com.example.fingerartbackend.mapper.ReviewMapper;
import com.example.fingerartbackend.mapper.ReviewReplyMapper;
import com.example.fingerartbackend.mapper.SkillExchangeRepository;
import com.example.fingerartbackend.mapper.SkillMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价服务实现类。
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private ReviewReplyMapper reviewReplyMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CustomOrderMapper orderMapper;
    @Autowired
    private SkillExchangeRepository exchangeRepository;
    @Autowired
    private SkillMapper skillMapper;
    @Autowired
    private ProductMapper productMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 提交评价。
     */
    @Override
    @Transactional
    public Review submitReview(Map<String, Object> payload) {
        Long fromUserId = Long.valueOf(payload.get("fromUserId").toString());
        Long toUserId = Long.valueOf(payload.get("toUserId").toString());
        Integer score = Integer.valueOf(payload.get("score").toString());
        String content = payload.get("content") != null ? payload.get("content").toString() : "";
        String imageUrlsJson = serializeImageUrls(payload.get("imageUrls"), 5);

        if (score < 1 || score > 5) {
            throw new RuntimeException("评分需在 1-5 之间");
        }

        User fromUser = userMapper.findById(fromUserId).orElseThrow(() -> new RuntimeException("评价人不存在"));
        User toUser = userMapper.findById(toUserId).orElseThrow(() -> new RuntimeException("被评价人不存在"));

        Review review = new Review();
        review.setFromUserId(fromUserId);
        review.setFromUserName(fromUser.getUsername());
        review.setFromUserAvatar(fromUser.getAvatar());
        review.setToUserId(toUserId);
        review.setToUserName(toUser.getUsername());
        review.setScore(score);
        review.setContent(content);
        review.setImageUrls(imageUrlsJson);

        if (payload.get("orderId") != null) {
            Long orderId = Long.valueOf(payload.get("orderId").toString());
            if (reviewMapper.existsByOrderIdAndFromUserId(orderId, fromUserId)) {
                throw new RuntimeException("您已评价过该订单");
            }
            CustomOrder order = orderMapper.findById(orderId).orElseThrow(() -> new RuntimeException("订单不存在"));
            if (!"COMPLETED".equals(order.getStatus())) {
                throw new RuntimeException("订单完成后才可评价");
            }
            if (!fromUserId.equals(order.getBuyerId()) && !fromUserId.equals(order.getArtisanId())) {
                throw new RuntimeException("无权评价该订单");
            }
            review.setOrderId(orderId);
            if (fromUserId.equals(order.getBuyerId()) && order.getProductId() != null) {
                review.setProductId(order.getProductId());
            }
            if (fromUserId.equals(order.getBuyerId())) {
                order.setBuyerReviewed(true);
            } else if (fromUserId.equals(order.getArtisanId())) {
                order.setArtisanReviewed(true);
            }
            orderMapper.save(order);
        }

        if (payload.get("exchangeId") != null) {
            Long exchangeId = Long.valueOf(payload.get("exchangeId").toString());
            if (reviewMapper.existsByExchangeIdAndFromUserId(exchangeId, fromUserId)) {
                throw new RuntimeException("您已评价过该交换");
            }
            SkillExchange exchange = exchangeRepository.findById(exchangeId)
                    .orElseThrow(() -> new RuntimeException("交换不存在"));
            if (!"COMPLETED".equals(exchange.getStatus())) {
                throw new RuntimeException("交换完成后才可评价");
            }
            review.setExchangeId(exchangeId);
            if (fromUserId.equals(exchange.getUserA().getId())) {
                exchange.setUserAReviewed(true);
            } else if (fromUserId.equals(exchange.getUserB().getId())) {
                exchange.setUserBReviewed(true);
            } else {
                throw new RuntimeException("无权评价该交换");
            }
            exchangeRepository.save(exchange);
        }

        Review saved = reviewMapper.save(review);
        applyCreditAndRating(toUser, score);
        syncSkillCredit(toUser);
        return saved;
    }

    /**
     * 查询评价信息。
     */
    @Override
    public List<Map<String, Object>> getReviewsForUser(Long userId) {
        return reviewMapper.findByToUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toUserReviewView)
                .collect(Collectors.toList());
    }

    /**
     * 执行 toUserReviewView 相关逻辑。
     */
    private Map<String, Object> toUserReviewView(Review review) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", review.getId());
        item.put("orderId", review.getOrderId());
        item.put("exchangeId", review.getExchangeId());
        item.put("productId", review.getProductId());
        item.put("fromUserId", review.getFromUserId());
        item.put("fromUserName", review.getFromUserName());
        item.put("fromUserAvatar", review.getFromUserAvatar());
        item.put("toUserId", review.getToUserId());
        item.put("toUserName", review.getToUserName());
        item.put("score", review.getScore());
        item.put("content", review.getContent());
        item.put("imageUrls", review.getImageUrls());
        item.put("createdAt", review.getCreatedAt());
        backfillFromUserProfile(item, review);

        if (review.getExchangeId() != null) {
            item.put("sourceType", "SKILL_EXCHANGE");
            item.put("sourceLabel", "技能交换");
            exchangeRepository.findById(review.getExchangeId()).ifPresent(exchange -> {
                item.put("contextTitle", resolveExchangeContextTitle(exchange));
            });
            if (!item.containsKey("contextTitle")) {
                item.put("contextTitle", "技能交换 #" + review.getExchangeId());
            }
            return item;
        }

        if (review.getOrderId() != null) {
            orderMapper.findById(review.getOrderId()).ifPresent(order -> enrichOrderReviewContext(item, review, order));
            if (!item.containsKey("sourceType")) {
                item.put("sourceType", "ORDER");
                item.put("sourceLabel", "订单交易");
            }
            if (!item.containsKey("contextTitle")) {
                item.put("contextTitle", "订单 #" + review.getOrderId());
            }
            return item;
        }

        item.put("sourceType", "GENERAL");
        item.put("sourceLabel", "交易评价");
        return item;
    }

    /**
     * 执行 enrichOrderReviewContext 相关逻辑。
     */
    private void enrichOrderReviewContext(Map<String, Object> item, Review review, CustomOrder order) {
        Long productId = review.getProductId() != null ? review.getProductId() : order.getProductId();
        boolean fromCustomRequest = order.getCustomRequestId() != null
                || "CUSTOM".equalsIgnoreCase(order.getProductType());
        if (productId != null) {
            item.put("sourceType", "PRODUCT_ORDER");
            item.put("sourceLabel", "作品交易");
            item.put("linkProductId", productId);
            String title = productMapper.findById(productId).map(Product::getTitle).orElse(null);
            item.put("contextTitle", (title != null && !title.isBlank()) ? title : fallbackOrderTitle(order));
        } else if (fromCustomRequest) {
            item.put("sourceType", "CUSTOM_ORDER");
            item.put("sourceLabel", "定制订单");
            item.put("contextTitle", fallbackOrderTitle(order));
        } else {
            item.put("sourceType", "ORDER");
            item.put("sourceLabel", "订单交易");
            item.put("contextTitle", fallbackOrderTitle(order));
        }
    }

    /**
     * 执行 fallbackOrderTitle 相关逻辑。
     */
    private String fallbackOrderTitle(CustomOrder order) {
        if (order.getProductTitle() != null && !order.getProductTitle().isBlank()) {
            return order.getProductTitle();
        }
        return "订单 #" + order.getId();
    }

    /**
     * 执行 resolveExchangeContextTitle 相关逻辑。
     */
    private String resolveExchangeContextTitle(SkillExchange exchange) {
        if (exchange.getDescription() != null && !exchange.getDescription().isBlank()) {
            String desc = exchange.getDescription().trim();
            return desc.length() > 48 ? desc.substring(0, 48) + "…" : desc;
        }
        return "技能交换 #" + exchange.getId();
    }

    /** 评价列表展示时使用用户当前头像/昵称，避免历史快照为空或失效 */
    private void backfillFromUserProfile(Map<String, Object> item, Review review) {
        if (review.getFromUserId() == null) {
            return;
        }
        userMapper.findById(review.getFromUserId()).ifPresent(user -> {
            if (user.getAvatar() != null && !user.getAvatar().isBlank()) {
                item.put("fromUserAvatar", user.getAvatar());
            }
            if (user.getUsername() != null && !user.getUsername().isBlank()) {
                item.put("fromUserName", user.getUsername());
            }
        });
    }

    /**
     * 查询评价信息。
     */
    @Override
    public List<Map<String, Object>> getProductReviews(Long productId) {
        if (productId == null) {
            return List.of();
        }
        List<Review> reviews = reviewMapper.findByProductIdOrderByCreatedAtDesc(productId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Review review : reviews) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", review.getId());
            item.put("orderId", review.getOrderId());
            item.put("exchangeId", review.getExchangeId());
            item.put("productId", review.getProductId());
            item.put("fromUserId", review.getFromUserId());
            item.put("fromUserName", review.getFromUserName());
            item.put("fromUserAvatar", review.getFromUserAvatar());
            item.put("toUserId", review.getToUserId());
            item.put("toUserName", review.getToUserName());
            item.put("score", review.getScore());
            item.put("content", review.getContent());
            item.put("imageUrls", review.getImageUrls());
            item.put("createdAt", review.getCreatedAt());
            backfillFromUserProfile(item, review);
            item.put("replies", reviewReplyMapper.findByReviewIdOrderByCreatedAtAsc(review.getId()));
            result.add(item);
        }
        return result;
    }

    /**
     * 查询评价信息。
     */
    @Override
    public Map<String, Object> getProductReviewEligibility(Long productId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("canReview", false);
        result.put("pendingOrders", List.of());
        if (productId == null || userId == null) {
            return result;
        }
        List<CustomOrder> completed = orderMapper
                .findByBuyerIdAndProductIdAndStatusOrderByCreateTimeDesc(userId, productId, "COMPLETED");
        List<Map<String, Object>> pending = completed.stream()
                .filter(o -> !reviewMapper.existsByOrderIdAndFromUserId(o.getId(), userId))
                .map(o -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("orderId", o.getId());
                    item.put("createTime", o.getCreateTime());
                    return item;
                })
                .collect(Collectors.toList());
        result.put("pendingOrders", pending);
        result.put("canReview", !pending.isEmpty());
        return result;
    }

    /**
     * 判断是否包含/拥有。
     */
    @Override
    public boolean hasReviewedOrder(Long orderId, Long fromUserId) {
        return reviewMapper.existsByOrderIdAndFromUserId(orderId, fromUserId);
    }

    /**
     * 判断是否包含/拥有。
     */
    @Override
    public boolean hasReviewedExchange(Long exchangeId, Long fromUserId) {
        return reviewMapper.existsByExchangeIdAndFromUserId(exchangeId, fromUserId);
    }

    /**
     * 完成评价。
     */
    @Override
    @Transactional
    public SkillExchange completeExchangeWithReview(Long exchangeId) {
        return exchangeRepository.findById(exchangeId).orElseThrow(() -> new RuntimeException("交换不存在"));
    }

    /**
     * 查询评价信息。
     */
    @Override
    public Map<String, Object> getOrderReviewDetail(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("review", null);
        result.put("replies", List.of());
        if (orderId == null || userId == null) {
            return result;
        }
        CustomOrder order = orderMapper.findById(orderId).orElse(null);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getArtisanId())) {
            throw new RuntimeException("无权查看该订单评价");
        }
        Review review = reviewMapper.findByOrderIdAndFromUserId(orderId, userId).orElse(null);
        if (review == null) {
            return result;
        }
        List<ReviewReply> replies = reviewReplyMapper.findByReviewIdOrderByCreatedAtAsc(review.getId());
        result.put("review", review);
        result.put("replies", replies);
        return result;
    }

    /**
     * 删除评价。
     */
    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewMapper.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        if (!userId.equals(review.getFromUserId())) {
            throw new RuntimeException("只能删除自己的评价");
        }
        reviewReplyMapper.deleteByReviewId(reviewId);
        reviewMapper.delete(review);
        if (review.getOrderId() != null) {
            orderMapper.findById(review.getOrderId()).ifPresent(order -> {
                if (userId.equals(order.getBuyerId())) {
                    order.setBuyerReviewed(false);
                } else if (userId.equals(order.getArtisanId())) {
                    order.setArtisanReviewed(false);
                }
                orderMapper.save(order);
            });
        }
    }

    /**
     * 执行 appendReviewReply 相关逻辑。
     */
    @Override
    @Transactional
    public ReviewReply appendReviewReply(Long reviewId, Long userId, String content, Object imageUrlsRaw) {
        String trimmed = content != null ? content.trim() : "";
        String imageUrlsJson = serializeImageUrls(imageUrlsRaw, 5);
        if (trimmed.isEmpty() && imageUrlsJson == null) {
            throw new RuntimeException("追评内容或图片至少填写一项");
        }
        if (trimmed.length() > 500) {
            throw new RuntimeException("追评内容不能超过 500 字");
        }
        Review review = reviewMapper.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        if (!userId.equals(review.getFromUserId())) {
            throw new RuntimeException("只能在自己的评价下追评");
        }
        User user = userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setFromUserId(userId);
        reply.setFromUserName(user.getUsername());
        reply.setContent(trimmed.isEmpty() ? null : trimmed);
        reply.setImageUrls(imageUrlsJson);
        return reviewReplyMapper.save(reply);
    }

    /**
     * 删除评价。
     */
    @Override
    @Transactional
    public void deleteReviewReply(Long replyId, Long userId) {
        ReviewReply reply = reviewReplyMapper.findById(replyId)
                .orElseThrow(() -> new RuntimeException("追评不存在"));
        if (!userId.equals(reply.getFromUserId())) {
            throw new RuntimeException("只能删除自己的追评");
        }
        reviewReplyMapper.delete(reply);
    }

    /**
     * 执行 applyCreditAndRating 相关逻辑。
     */
    private void applyCreditAndRating(User user, int score) {
        int credit = user.getCreditScore() != null ? user.getCreditScore() : 100;
        if (score >= 4) credit += 2;
        else if (score <= 2) credit -= 5;
        else credit += 1;
        user.setCreditScore(Math.max(0, Math.min(200, credit)));

        int count = user.getReviewCount() != null ? user.getReviewCount() : 0;
        double oldRating = user.getRating() != null ? user.getRating() : 5.0;
        double newRating = ((oldRating * count) + score) / (count + 1);
        user.setRating(Math.round(newRating * 100.0) / 100.0);
        user.setReviewCount(count + 1);
        userMapper.save(user);
    }

    /**
     * 执行 syncSkillCredit 相关逻辑。
     */
    private void syncSkillCredit(User user) {
        List<Skill> skills = skillMapper.findByUserId(user.getId());
        for (Skill s : skills) {
            s.setCredit(user.getCreditScore());
            s.setRating(user.getRating());
            skillMapper.save(s);
        }
    }

    /**
     * 执行 serializeImageUrls 相关逻辑。
     */
    private String serializeImageUrls(Object raw) {
        return serializeImageUrls(raw, 5);
    }

    /**
     * 执行 serializeImageUrls 相关逻辑。
     */
    private String serializeImageUrls(Object raw, int maxCount) {
        if (raw == null) {
            return null;
        }
        try {
            List<String> urls = new ArrayList<>();
            if (raw instanceof List<?> list) {
                for (Object item : list) {
                    if (item != null && !item.toString().isBlank()) {
                        urls.add(item.toString().trim());
                    }
                }
            } else if (raw instanceof String str && !str.isBlank()) {
                if (str.trim().startsWith("[")) {
                    List<String> parsed = objectMapper.readValue(str, new TypeReference<List<String>>() {});
                    urls.addAll(parsed.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).toList());
                } else {
                    urls.add(str.trim());
                }
            }
            if (urls.isEmpty()) {
                return null;
            }
            if (urls.size() > maxCount) {
                throw new RuntimeException("图片最多 " + maxCount + " 张");
            }
            return objectMapper.writeValueAsString(urls);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("评价图片格式无效");
        }
    }
}
