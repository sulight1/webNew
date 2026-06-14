package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.CustomRequest;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.CustomRequestMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.CustomRequestBidService;
import com.example.fingerartbackend.service.DemandMatchService;
import com.example.fingerartbackend.dto.CustomRequestBidView;
import com.example.fingerartbackend.dto.SelectBidResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/custom-requests")
public class CustomRequestController {

    @Autowired
    private CustomRequestMapper requestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DemandMatchService demandMatchService;

    @Autowired
    private CustomRequestBidService bidService;

    @GetMapping
    public Result<List<CustomRequest>> findAll() {
        return Result.success(requestMapper.findAll());
    }

    @GetMapping("/bids/by-artisan/{artisanId}")
    public Result<List<Long>> myBidRequestIds(@PathVariable Long artisanId) {
        return Result.success(bidService.listBidRequestIdsByArtisan(artisanId));
    }

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

    @GetMapping("/{id}/bids/count")
    public Result<Long> bidCount(@PathVariable Long id, @RequestParam Long viewerUserId) {
        try {
            bidService.listBidsForRequest(id, viewerUserId);
            return Result.success(bidService.countPendingBids(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

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

    @PostMapping
    public Result<CustomRequest> create(@RequestBody Map<String, Object> payload) {
        try {
            CustomRequest request = new CustomRequest();
            request.setTitle((String) payload.get("title"));
            request.setCategory((String) payload.get("category"));
            request.setDescription((String) payload.get("description"));
            request.setBudgetMin(Double.valueOf(payload.get("budgetMin").toString()));
            request.setBudgetMax(Double.valueOf(payload.get("budgetMax").toString()));
            request.setDeadline((String) payload.get("deadline"));
            if (payload.get("referenceImage") != null) {
                String ref = payload.get("referenceImage").toString().trim();
                if (!ref.isEmpty()) {
                    request.setReferenceImage(ref);
                }
            }
            
            Long buyerId = Long.valueOf(payload.get("buyerId").toString());
            User buyer = userMapper.findById(buyerId).orElseThrow(() -> new RuntimeException("用户不存在"));
            request.setBuyer(buyer);
            
            CustomRequest saved = requestMapper.save(request);
            demandMatchService.notifyMatchedArtisans(saved);
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发布失败: " + e.getMessage());
        }
    }

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
