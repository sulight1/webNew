package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.LogisticsTraceResult;
import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.EscrowTransaction;
import com.example.fingerartbackend.entity.OrderMilestone;
import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.LogisticsService;
import com.example.fingerartbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminAuditService adminAuditService;

    @Autowired
    private LogisticsService logisticsService;

    @PostMapping
    public Result<CustomOrder> createOrder(@RequestBody CustomOrder order) {
        try {
            return Result.success(orderService.createOrder(order));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<CustomOrder>> getAllOrders(@RequestParam(required = false) String status) {
        try {
            return Result.success(orderService.getAllOrders(status));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<CustomOrder> getOrder(@PathVariable Long id) {
        try {
            return Result.success(orderService.getOrder(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/artisan/{id}")
    public Result<List<CustomOrder>> getArtisanOrders(@PathVariable Long id) {
        try {
            return Result.success(orderService.getArtisanOrders(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/buyer/{id}")
    public Result<List<CustomOrder>> getBuyerOrders(@PathVariable Long id) {
        try {
            return Result.success(orderService.getBuyerOrders(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm")
    public Result<CustomOrder> confirmOrder(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(orderService.confirmOrder(id, body.get("artisanId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/pay-deposit")
    public Result<CustomOrder> payDeposit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long buyerId = Long.valueOf(body.get("buyerId").toString());
            String paymentChannel = body.get("paymentChannel") != null
                    ? body.get("paymentChannel").toString()
                    : "ZAOWU_COIN";
            return Result.success(orderService.payDeposit(id, buyerId, paymentChannel));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/pay-balance")
    public Result<CustomOrder> payBalance(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(orderService.payBalance(id, body.get("buyerId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm-receipt")
    public Result<CustomOrder> confirmReceipt(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(orderService.confirmReceipt(id, body.get("buyerId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/ship")
    public Result<CustomOrder> shipOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long artisanId = Long.valueOf(body.get("artisanId").toString());
            String shippingCompany = body.get("shippingCompany") != null ? body.get("shippingCompany").toString() : null;
            String trackingNumber = body.get("trackingNumber") != null ? body.get("trackingNumber").toString() : null;
            String operatorName = body.get("operatorName") != null ? body.get("operatorName").toString() : null;
            return Result.success(orderService.shipOrder(id, artisanId, shippingCompany, trackingNumber, operatorName));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<CustomOrder> updateStatus(@PathVariable Long id, @RequestParam String status,
                                            @RequestParam(required = false) Long operatorId,
                                            @RequestParam(required = false) String operatorName) {
        try {
            if ("RECEIVED".equals(status)) {
                status = "PENDING_ACCEPT";
            }
            return Result.success(orderService.updateStatus(id, status, operatorId, operatorName != null ? operatorName : "系统"));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/milestones")
    public Result<List<OrderMilestone>> getMilestones(@PathVariable Long id) {
        try {
            return Result.success(orderService.getMilestones(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/milestones")
    public Result<CustomOrder> addMilestone(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Long operatorId = body.get("operatorId") != null ? Long.valueOf(body.get("operatorId")) : null;
            String operatorName = body.get("operatorName");
            return Result.success(orderService.addMilestone(id, body, operatorId, operatorName));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/logistics")
    public Result<LogisticsTraceResult> getOrderLogistics(@PathVariable Long id) {
        try {
            return Result.success(logisticsService.queryOrderLogistics(id, AuthContext.getUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/escrow")
    public Result<List<EscrowTransaction>> getEscrow(@PathVariable Long id) {
        try {
            return Result.success(orderService.getEscrowTransactions(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/dispute")
    public Result<CustomOrder> openDispute(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String reason = body.get("reason") != null ? body.get("reason").toString() : null;
            return Result.success(orderService.openDispute(id, userId, reason));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/resolve-dispute")
    public Result<CustomOrder> resolveDispute(@PathVariable Long id, @RequestParam boolean releaseToArtisan) {
        try {
            CustomOrder order = orderService.resolveDispute(id, releaseToArtisan);
            adminAuditService.log("RESOLVE_DISPUTE", "ORDER", id,
                    (releaseToArtisan ? "纠纷放款给达人" : "纠纷退款给买家") + "，订单 " + order.getProductTitle());
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/request-cancel")
    public Result<CustomOrder> requestCancel(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long buyerId = Long.valueOf(body.get("buyerId").toString());
            String reason = body.get("reason") != null ? body.get("reason").toString() : null;
            return Result.success(orderService.requestCancel(id, buyerId, reason));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve-cancel")
    public Result<CustomOrder> approveCancel(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(orderService.approveCancel(id, body.get("artisanId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject-cancel")
    public Result<CustomOrder> rejectCancel(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long artisanId = Long.valueOf(body.get("artisanId").toString());
            String reason = body.get("reason") != null ? body.get("reason").toString() : null;
            return Result.success(orderService.rejectCancel(id, artisanId, reason));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return Result.success("订单删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
