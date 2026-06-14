package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.Review;
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

    @GetMapping("/check-order")
    public Result<Boolean> checkOrderReview(@RequestParam Long orderId, @RequestParam Long fromUserId) {
        return Result.success(reviewService.hasReviewedOrder(orderId, fromUserId));
    }

    @GetMapping("/check-exchange")
    public Result<Boolean> checkExchangeReview(@RequestParam Long exchangeId, @RequestParam Long fromUserId) {
        return Result.success(reviewService.hasReviewedExchange(exchangeId, fromUserId));
    }
}
