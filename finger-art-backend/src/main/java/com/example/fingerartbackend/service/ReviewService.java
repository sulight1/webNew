package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.Review;
import com.example.fingerartbackend.entity.ReviewReply;
import com.example.fingerartbackend.entity.SkillExchange;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    Review submitReview(Map<String, Object> payload);
    List<Review> getReviewsForUser(Long userId);
    List<Map<String, Object>> getProductReviews(Long productId);
    Map<String, Object> getProductReviewEligibility(Long productId, Long userId);
    boolean hasReviewedOrder(Long orderId, Long fromUserId);
    boolean hasReviewedExchange(Long exchangeId, Long fromUserId);
    SkillExchange completeExchangeWithReview(Long exchangeId);
    Map<String, Object> getOrderReviewDetail(Long orderId, Long userId);
    void deleteReview(Long reviewId, Long userId);
    void deleteReviewReply(Long replyId, Long userId);
    ReviewReply appendReviewReply(Long reviewId, Long userId, String content, Object imageUrls);
}
