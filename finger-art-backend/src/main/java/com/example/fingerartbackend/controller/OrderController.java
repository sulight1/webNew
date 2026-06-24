package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.BatchCheckoutRequest;
import com.example.fingerartbackend.dto.BatchCheckoutResult;
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
import java.util.Objects;

/**
 * 订单控制器。
 * 负责订单创建、支付、发货、里程碑、纠纷及取消全流程，对应订单与交易管理模块。
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminAuditService adminAuditService;

    @Autowired
    private LogisticsService logisticsService;

    /**
     * 创建新订单。
     *
     * @param order 订单实体
     * @return 创建成功的订单
     */
    @PostMapping
    public Result<CustomOrder> createOrder(@RequestBody CustomOrder order) {
        try {
            return Result.success(orderService.createOrder(order));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询全部订单，可按状态筛选。
     *
     * @param status 可选订单状态
     * @return 订单列表
     */
    @GetMapping
    public Result<List<CustomOrder>> getAllOrders(@RequestParam(required = false) String status) {
        try {
            return Result.success(orderService.getAllOrders(status));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量结算现货商品，生成多个订单。
     *
     * @param request 批量结算请求（买家 ID、商品项列表等）
     * @return 批量结算结果
     */
    @PostMapping("/batch-checkout")
    public Result<BatchCheckoutResult> batchCheckout(@RequestBody BatchCheckoutRequest request) {
        try {
            Long authUserId = AuthContext.getUserId();
            if (authUserId == null) {
                return Result.error("请先登录");
            }
            if (request.getBuyerId() == null || !Objects.equals(request.getBuyerId(), authUserId)) {
                return Result.error("无权为其他用户结算");
            }
            return Result.success(orderService.batchCheckoutReadyMade(request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 按 ID 查询订单详情。
     *
     * @param id 订单 ID
     * @return 订单实体
     */
    @GetMapping("/{id}")
    public Result<CustomOrder> getOrder(@PathVariable Long id) {
        try {
            return Result.success(orderService.getOrder(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询手作人相关的全部订单。
     *
     * @param id 手作人用户 ID
     * @return 订单列表
     */
    @GetMapping("/artisan/{id}")
    public Result<List<CustomOrder>> getArtisanOrders(@PathVariable Long id) {
        try {
            return Result.success(orderService.getArtisanOrders(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询买家相关的全部订单。
     *
     * @param id 买家用户 ID
     * @return 订单列表
     */
    @GetMapping("/buyer/{id}")
    public Result<List<CustomOrder>> getBuyerOrders(@PathVariable Long id) {
        try {
            return Result.success(orderService.getBuyerOrders(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 手作人确认接单。
     *
     * @param id   订单 ID
     * @param body 含 artisanId 的请求体
     * @return 更新后的订单
     */
    @PostMapping("/{id}/confirm")
    public Result<CustomOrder> confirmOrder(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(orderService.confirmOrder(id, body.get("artisanId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 买家支付订单定金。
     *
     * @param id   订单 ID
     * @param body 含 buyerId、paymentChannel 的请求体
     * @return 更新后的订单
     */
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

    /**
     * 买家支付订单尾款。
     *
     * @param id   订单 ID
     * @param body 含 buyerId 的请求体
     * @return 更新后的订单
     */
    @PostMapping("/{id}/pay-balance")
    public Result<CustomOrder> payBalance(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(orderService.payBalance(id, body.get("buyerId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 买家确认收货。
     *
     * @param id   订单 ID
     * @param body 含 buyerId 的请求体
     * @return 更新后的订单
     */
    @PostMapping("/{id}/confirm-receipt")
    public Result<CustomOrder> confirmReceipt(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(orderService.confirmReceipt(id, body.get("buyerId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 手作人发货并填写物流信息。
     *
     * @param id   订单 ID
     * @param body 含 artisanId、shippingCompany、trackingNumber 的请求体
     * @return 更新后的订单
     */
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

    /**
     * 更新订单状态。
     *
     * @param id           订单 ID
     * @param status       目标状态
     * @param operatorId   可选操作者 ID
     * @param operatorName 可选操作者名称
     * @return 更新后的订单
     */
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

    /**
     * 查询订单制作里程碑列表。
     *
     * @param id 订单 ID
     * @return 里程碑列表
     */
    @GetMapping("/{id}/milestones")
    public Result<List<OrderMilestone>> getMilestones(@PathVariable Long id) {
        try {
            return Result.success(orderService.getMilestones(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为订单添加制作里程碑。
     *
     * @param id   订单 ID
     * @param body 里程碑字段及 operatorId、operatorName
     * @return 更新后的订单
     */
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

    /**
     * 查询订单物流轨迹。
     *
     * @param id 订单 ID
     * @return 物流追踪结果
     */
    @GetMapping("/{id}/logistics")
    public Result<LogisticsTraceResult> getOrderLogistics(@PathVariable Long id) {
        try {
            return Result.success(logisticsService.queryOrderLogistics(id, AuthContext.getUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询订单托管资金流水。
     *
     * @param id 订单 ID
     * @return 托管交易记录列表
     */
    @GetMapping("/{id}/escrow")
    public Result<List<EscrowTransaction>> getEscrow(@PathVariable Long id) {
        try {
            return Result.success(orderService.getEscrowTransactions(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发起订单纠纷。
     *
     * @param id   订单 ID
     * @param body 含 userId、reason 的请求体
     * @return 更新后的订单
     */
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

    /**
     * 管理员裁决订单纠纷。
     *
     * @param id              订单 ID
     * @param releaseToArtisan 是否放款给手作人，false 则退款给买家
     * @return 更新后的订单
     */
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

    /**
     * 买家申请取消订单。
     *
     * @param id   订单 ID
     * @param body 含 buyerId、reason 的请求体
     * @return 更新后的订单
     */
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

    /**
     * 手作人同意取消订单。
     *
     * @param id   订单 ID
     * @param body 含 artisanId 的请求体
     * @return 更新后的订单
     */
    @PostMapping("/{id}/approve-cancel")
    public Result<CustomOrder> approveCancel(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(orderService.approveCancel(id, body.get("artisanId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 手作人拒绝取消订单。
     *
     * @param id   订单 ID
     * @param body 含 artisanId、reason 的请求体
     * @return 更新后的订单
     */
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

    /**
     * 删除指定订单。
     *
     * @param id 订单 ID
     * @return 删除成功提示
     */
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
