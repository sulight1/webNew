package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.CustomRequestPageResult;
import com.example.fingerartbackend.entity.CustomRequest;
import com.example.fingerartbackend.mapper.CustomRequestMapper;
import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.CustomRequestBidService;
import com.example.fingerartbackend.service.CustomRequestService;
import com.example.fingerartbackend.dto.CustomRequestBidView;
import com.example.fingerartbackend.dto.SelectBidResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 定制需求控制器。
 * 负责定制需求的发布、查询、竞价、审核及状态管理，对应定制交易模块。
 */
@RestController
@RequestMapping("/custom-requests")
public class CustomRequestController {

    @Autowired
    private CustomRequestMapper requestMapper;

    @Autowired
    private CustomRequestBidService bidService;

    @Autowired
    private CustomRequestService customRequestService;

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 查询定制需求列表，支持分页、筛选与排序。
     *
     * @param page     页码，与 size 同时传入时启用分页
     * @param size     每页条数
     * @param status   需求状态，默认 OPEN
     * @param category 作品分类筛选
     * @param keyword  关键词搜索
     * @param sort     排序方式，默认 latest
     * @return 全量列表或分页搜索结果
     */
    @GetMapping
    public Result<?> findAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "OPEN") String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "latest") String sort) {
        if (page == null || size == null) {
            return Result.success(requestMapper.findAll());
        }
        return Result.success(customRequestService.search(status, category, keyword, sort, page, size));
    }

    /**
     * 按 ID 查询单条定制需求详情。
     *
     * @param id 需求 ID
     * @return 定制需求实体
     */
    @GetMapping("/{id}")
    public Result<CustomRequest> findById(@PathVariable Long id) {
        return requestMapper.findById(id)
                .map(Result::success)
                .orElseGet(() -> Result.error("需求不存在"));
    }

    /**
     * 查询手作人已竞价的需求 ID 列表。
     *
     * @param artisanId 手作人用户 ID
     * @return 已参与竞价的需求 ID 集合
     */
    @GetMapping("/bids/by-artisan/{artisanId}")
    public Result<List<Long>> myBidRequestIds(@PathVariable Long artisanId) {
        return Result.success(bidService.listBidRequestIdsByArtisan(artisanId));
    }

    /**
     * 查询指定需求的竞价列表。
     *
     * @param id           需求 ID
     * @param viewerUserId 查看者用户 ID，用于权限校验
     * @return 竞价视图列表
     */
    @GetMapping("/{id}/bids")
    public Result<List<CustomRequestBidView>> listBids(
            @PathVariable Long id,
            @RequestParam Long viewerUserId) {
        try {
            return Result.success(bidService.listBidsForRequest(id, viewerUserId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询指定需求的待处理竞价数量。
     *
     * @param id           需求 ID
     * @param viewerUserId 查看者用户 ID，用于权限校验
     * @return 待处理竞价条数
     */
    @GetMapping("/{id}/bids/count")
    public Result<Long> bidCount(@PathVariable Long id, @RequestParam Long viewerUserId) {
        try {
            bidService.listBidsForRequest(id, viewerUserId);
            return Result.success(bidService.countPendingBids(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 手作人提交竞价申请。
     *
     * @param id   需求 ID
     * @param body 含 artisanId、message 的请求体
     * @return 更新后的定制需求
     */
    @PostMapping("/{id}/bids")
    public Result<CustomRequest> createBid(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Long artisanId = Long.valueOf(body.get("artisanId").toString());
            String message = body.get("message") != null ? body.get("message").toString() : null;
            bidService.submitBid(id, artisanId, message);
            return Result.success(requestMapper.findById(id).orElseThrow());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 买家选定某一竞价并生成订单。
     *
     * @param id   需求 ID
     * @param body 含 buyerId、bidId 的请求体
     * @return 选标结果（含订单信息）
     */
    @PostMapping("/{id}/select-bid")
    public Result<SelectBidResult> selectBid(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Long buyerId = Long.valueOf(body.get("buyerId").toString());
            Long bidId = Long.valueOf(body.get("bidId").toString());
            return Result.success(bidService.selectBid(id, buyerId, bidId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布新的定制需求。
     *
     * @param payload 需求字段（标题、描述、预算等）
     * @return 创建成功的定制需求
     */
    @PostMapping
    public Result<CustomRequest> create(@RequestBody Map<String, Object> payload) {
        try {
            return Result.success(customRequestService.createRequest(payload));
        } catch (Exception e) {
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 管理员审核定制需求。
     *
     * @param id     需求 ID
     * @param status 审核结果状态
     * @return 审核后的定制需求
     */
    @PostMapping("/{id}/audit")
    public Result<CustomRequest> auditRequest(@PathVariable Long id, @RequestParam String status) {
        try {
            CustomRequest request = customRequestService.auditRequest(id, status);
            adminAuditService.log("AUDIT_CUSTOM_REQUEST", "CUSTOM_REQUEST", id,
                    "审核定制需求「" + request.getTitle() + "」为 " + status);
            return Result.success(request);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新定制需求状态。
     *
     * @param id   需求 ID
     * @param body 含 status 字段的请求体
     * @return 更新后的定制需求
     */
    @PatchMapping("/{id}/status")
    public Result<CustomRequest> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            CustomRequest request = requestMapper.findById(id).orElseThrow(() -> new RuntimeException("需求不存在"));
            request.setStatus(body.get("status"));
            return Result.success(requestMapper.save(request));
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定定制需求。
     *
     * @param id 需求 ID
     * @return 删除成功提示
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRequest(@PathVariable Long id) {
        try {
            requestMapper.deleteById(id);
            return Result.success("定制需求已删除");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
