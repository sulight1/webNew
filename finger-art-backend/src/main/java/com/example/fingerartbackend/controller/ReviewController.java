package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.entity.Review;
import com.example.fingerartbackend.entity.ReviewReply;
import com.example.fingerartbackend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public Result<Review> submit(@RequestBody Map<String, Object> payload) {
        try {
            return Result.success(reviewService.submitReview(payload));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<List<Review>> getUserReviews(@PathVariable Long userId) {
        return Result.success(reviewService.getReviewsForUser(userId));
    }

    @GetMapping("/product/{productId}")
    public Result<List<Map<String, Object>>> getProductReviews(@PathVariable Long productId) {
        return Result.success(reviewService.getProductReviews(productId));
    }

    @GetMapping("/product/{productId}/eligibility")
    public Result<Map<String, Object>> getProductReviewEligibility(@PathVariable Long productId) {
        try {
            Long userId = AuthContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(reviewService.getProductReviewEligibility(productId, userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/check-order")
    public Result<Boolean> checkOrderReview(@RequestParam Long orderId, @RequestParam Long fromUserId) {
        return Result.success(reviewService.hasReviewedOrder(orderId, fromUserId));
    }

    @GetMapping("/check-exchange")
    public Result<Boolean> checkExchangeReview(@RequestParam Long exchangeId, @RequestParam Long fromUserId) {
        return Result.success(reviewService.hasReviewedExchange(exchangeId, fromUserId));
    }

    @GetMapping("/order/{orderId}")
    public Result<Map<String, Object>> getOrderReview(@PathVariable Long orderId) {
        try {
            Long userId = AuthContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(reviewService.getOrderReviewDetail(orderId, userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(@PathVariable Long reviewId) {
        try {
            Long userId = AuthContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            reviewService.deleteReview(reviewId, userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/replies/{replyId}")
    public Result<Void> deleteReply(@PathVariable Long replyId) {
        try {
            Long userId = AuthContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            reviewService.deleteReviewReply(replyId, userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{reviewId}/replies")
    public Result<ReviewReply> appendReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> payload) {
        try {
            Long userId = AuthContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            String content = payload.get("content") != null ? payload.get("content").toString() : "";
            Object imageUrls = payload.get("imageUrls");
            return Result.success(reviewService.appendReviewReply(reviewId, userId, content, imageUrls));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
