package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.Review;
import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.entity.SkillExchange;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.CustomOrderMapper;
import com.example.fingerartbackend.mapper.ReviewMapper;
import com.example.fingerartbackend.mapper.SkillExchangeRepository;
import com.example.fingerartbackend.mapper.SkillMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CustomOrderMapper orderMapper;
    @Autowired
    private SkillExchangeRepository exchangeRepository;
    @Autowired
    private SkillMapper skillMapper;

    @Override
    @Transactional
    public Review submitReview(Map<String, Object> payload) {
        Long fromUserId = Long.valueOf(payload.get("fromUserId").toString());
        Long toUserId = Long.valueOf(payload.get("toUserId").toString());
        Integer score = Integer.valueOf(payload.get("score").toString());
        String content = payload.get("content") != null ? payload.get("content").toString() : "";

        if (score < 1 || score > 5) {
            throw new RuntimeException("评分需在 1-5 之间");
        }

        User fromUser = userMapper.findById(fromUserId).orElseThrow(() -> new RuntimeException("评价人不存在"));
        User toUser = userMapper.findById(toUserId).orElseThrow(() -> new RuntimeException("被评价人不存在"));

        Review review = new Review();
        review.setFromUserId(fromUserId);
        review.setFromUserName(fromUser.getUsername());
        review.setToUserId(toUserId);
        review.setToUserName(toUser.getUsername());
        review.setScore(score);
        review.setContent(content);

        if (payload.get("orderId") != null) {
            Long orderId = Long.valueOf(payload.get("orderId").toString());
            if (reviewMapper.existsByOrderIdAndFromUserId(orderId, fromUserId)) {
                throw new RuntimeException("您已评价过该订单");
            }
            CustomOrder order = orderMapper.findById(orderId).orElseThrow(() -> new RuntimeException("订单不存在"));
            if (!"COMPLETED".equals(order.getStatus())) {
                throw new RuntimeException("订单完成后才可评价");
            }
            review.setOrderId(orderId);
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
}
