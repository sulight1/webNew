package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.Review;
import com.example.fingerartbackend.entity.ReviewReply;
import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.entity.SkillExchange;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.CustomOrderMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Override
    public List<Review> getReviewsForUser(Long userId) {
        return reviewMapper.findByToUserIdOrderByCreatedAtDesc(userId);
    }

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
            item.put("replies", reviewReplyMapper.findByReviewIdOrderByCreatedAtAsc(review.getId()));
            result.add(item);
        }
        return result;
    }

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

    @Override
    public boolean hasReviewedOrder(Long orderId, Long fromUserId) {
        return reviewMapper.existsByOrderIdAndFromUserId(orderId, fromUserId);
    }

    @Override
    public boolean hasReviewedExchange(Long exchangeId, Long fromUserId) {
        return reviewMapper.existsByExchangeIdAndFromUserId(exchangeId, fromUserId);
    }

    @Override
    @Transactional
    public SkillExchange completeExchangeWithReview(Long exchangeId) {
        return exchangeRepository.findById(exchangeId).orElseThrow(() -> new RuntimeException("交换不存在"));
    }

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

    private void syncSkillCredit(User user) {
        List<Skill> skills = skillMapper.findByUserId(user.getId());
        for (Skill s : skills) {
            s.setCredit(user.getCreditScore());
            s.setRating(user.getRating());
            skillMapper.save(s);
        }
    }

    private String serializeImageUrls(Object raw) {
        return serializeImageUrls(raw, 5);
    }

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
