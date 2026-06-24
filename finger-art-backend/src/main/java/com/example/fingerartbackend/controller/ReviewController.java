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

/**
 * 评价控制器。
 * 处理订单/交换/作品评价的提交、查询、追评及删除，对应信用与口碑模块。
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 提交评价（订单、交换或作品）。
     *
     * @param payload 评价字段（评分、内容、关联 ID 等）
     * @return 创建的评价记录
     */
    @PostMapping
    public Result<Review> submit(@RequestBody Map<String, Object> payload) {
        try {
            return Result.success(reviewService.submitReview(payload));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询用户收到的全部评价。
     *
     * @param userId 被评价用户 ID
     * @return 评价详情列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserReviews(@PathVariable Long userId) {
        return Result.success(reviewService.getReviewsForUser(userId));
    }

    /**
     * 查询作品下的全部评价。
     *
     * @param productId 作品 ID
     * @return 评价详情列表
     */
    @GetMapping("/product/{productId}")
    public Result<List<Map<String, Object>>> getProductReviews(@PathVariable Long productId) {
        return Result.success(reviewService.getProductReviews(productId));
    }

    /**
     * 查询当前用户对指定作品是否具备评价资格。
     *
     * @param productId 作品 ID
     * @return 资格信息（是否可评、原因等）
     */
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

    /**
     * 检查用户是否已对指定订单提交评价。
     *
     * @param orderId    订单 ID
     * @param fromUserId 评价发起用户 ID
     * @return 是否已评价
     */
    @GetMapping("/check-order")
    public Result<Boolean> checkOrderReview(@RequestParam Long orderId, @RequestParam Long fromUserId) {
        return Result.success(reviewService.hasReviewedOrder(orderId, fromUserId));
    }

    /**
     * 检查用户是否已对指定技能交换提交评价。
     *
     * @param exchangeId 交换记录 ID
     * @param fromUserId 评价发起用户 ID
     * @return 是否已评价
     */
    @GetMapping("/check-exchange")
    public Result<Boolean> checkExchangeReview(@RequestParam Long exchangeId, @RequestParam Long fromUserId) {
        return Result.success(reviewService.hasReviewedExchange(exchangeId, fromUserId));
    }

    /**
     * 查询指定订单的评价详情。
     *
     * @param orderId 订单 ID
     * @return 评价详情 Map
     */
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

    /**
     * 删除指定评价（仅作者可操作）。
     *
     * @param reviewId 评价 ID
     * @return 空成功响应
     */
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

    /**
     * 删除评价追评/回复（仅作者可操作）。
     *
     * @param replyId 追评 ID
     * @return 空成功响应
     */
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

    /**
     * 为评价追加回复（追评）。
     *
     * @param reviewId 评价 ID
     * @param payload  含 content、imageUrls 的请求体
     * @return 创建的追评记录
     */
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
