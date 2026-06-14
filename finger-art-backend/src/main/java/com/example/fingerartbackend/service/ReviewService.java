package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.Review;
import com.example.fingerartbackend.entity.SkillExchange;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    Review submitReview(Map<String, Object> payload);
    List<Review> getReviewsForUser(Long userId);
    boolean hasReviewedOrder(Long orderId, Long fromUserId);
    boolean hasReviewedExchange(Long exchangeId, Long fromUserId);
    SkillExchange completeExchangeWithReview(Long exchangeId);
}
