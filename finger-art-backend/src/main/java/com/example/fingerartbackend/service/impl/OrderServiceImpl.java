package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.*;
import com.example.fingerartbackend.mapper.*;
import com.example.fingerartbackend.service.CoinEconomyService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.OrderService;
import com.example.fingerartbackend.service.UserPunishmentService;
import com.example.fingerartbackend.constant.UserPunishmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final double DEPOSIT_RATIO = 0.3;

    @Autowired
    private CustomOrderMapper orderMapper;
    @Autowired
    private OrderMilestoneMapper milestoneMapper;
    @Autowired
    private EscrowTransactionMapper escrowMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CoinEconomyService coinEconomyService;
    @Autowired
    private UserPunishmentService userPunishmentService;

    @Override
    @Transactional
    public CustomOrder createOrder(CustomOrder order) {
        if (order.getBuyerId() != null) {
            userPunishmentService.assertNotPunished(order.getBuyerId(), UserPunishmentType.NO_ORDER, "您已被禁止下单");
        }
        if (order.getArtisanId() == null) {
            throw new RuntimeException("创作者ID不能为空");
        }
        if (order.getQuantity() == null || order.getQuantity() < 1) {
            order.setQuantity(1);
        }
        if ((order.getProductType() == null || order.getProductType().isBlank()) && order.getProductId() != null) {
            productMapper.findById(order.getProductId()).ifPresent(product -> {
                if (product.getType() != null && !product.getType().isBlank()) {
                    order.setProductType(product.getType());
                }
            });
        }
        boolean readyMade = isReadyMadeOrder(order);
        if (readyMade) {
            order.setDepositRatio(1.0);
            if (order.getPrice() != null) {
                order.setDepositAmount(round(order.getPrice()));
                order.setBalanceAmount(0.0);
            }
        } else {
            order.setDepositRatio(DEPOSIT_RATIO);
            if (order.getPrice() != null) {
                order.setDepositAmount(round(order.getPrice() * DEPOSIT_RATIO));
                order.setBalanceAmount(round(order.getPrice() - order.getDepositAmount()));
            }
        }
        order.setEscrowAmount(0.0);
        order.setEscrowStatus("NONE");
        order.setBuyerReviewed(false);
        order.setArtisanReviewed(false);
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            order.setStatus(readyMade ? "PENDING_PAY" : "PENDING_CONFIRM");
        }
        applyShippingFromBuyer(order);
        if (readyMade) {
            requireOrderShippingAddress(order);
        }
        validateProductStock(order);
        CustomOrder saved = orderMapper.save(order);
        if (readyMade) {
            addMilestoneRecord(saved.getId(), "ORDER", "下单成功", "成品订单已创建，等待买家支付全款", null,
                    order.getBuyerId(), order.getBuyerName());
            notifyBuyer(saved, "订单待支付", "请支付全款完成购买「" + safeTitle(order) + "」");
            notifyArtisan(saved, "新成品订单", "买家「" + safeName(order.getBuyerName()) + "」下单「" + safeTitle(order) + "」，等待付款");
        } else {
            String confirmNote = buildCustomRequirementsMilestoneNote(order.getRequirements());
            addMilestoneRecord(saved.getId(), "CONFIRM", "确认需求", confirmNote, null,
                    order.getBuyerId(), order.getBuyerName());
            notifyArtisan(saved, "新订单待确认", buildCustomOrderNotifyContent(order));
        }
        return saved;
    }

    @Override
    public List<CustomOrder> getAllOrders(String status) {
        if (status != null && !status.isEmpty()) {
            return orderMapper.findByStatusOrderByCreateTimeDesc(status);
        }
        return orderMapper.findAll(Sort.by(Sort.Direction.DESC, "createTime"));
    }

    @Override
    public CustomOrder getOrder(Long id) {
        CustomOrder order = orderMapper.findById(id).orElseThrow(() -> new RuntimeException("订单不存在"));
        return normalizeOrderForRead(order);
    }

    @Override
    public List<CustomOrder> getArtisanOrders(Long artisanId) {
        return orderMapper.findByArtisanId(artisanId).stream().map(this::normalizeOrderForRead).toList();
    }

    @Override
    public List<CustomOrder> getBuyerOrders(Long buyerId) {
        return orderMapper.findByBuyerIdOrderByCreateTimeDesc(buyerId).stream().map(this::normalizeOrderForRead).toList();
    }

    @Override
    @Transactional
    public CustomOrder confirmOrder(Long orderId, Long artisanId) {
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getArtisanId(), artisanId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PENDING_CONFIRM".equals(order.getStatus()) && !"PENDING_PAY".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可确认");
        }
        order.setStatus("PENDING_PAY");
        CustomOrder saved = orderMapper.save(order);
        User artisan = userMapper.findById(artisanId).orElse(null);
        addMilestoneRecord(orderId, "CONFIRM", "确认需求", "手作人已确认定制需求", null, artisanId,
                artisan != null ? artisan.getUsername() : order.getArtisanName());
        notifyBuyer(saved, "订单已确认", "手作人已确认「" + safeTitle(order) + "」，请支付定金");
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder payDeposit(Long orderId, Long buyerId, String paymentChannel) {
        userPunishmentService.assertNotPunished(buyerId, UserPunishmentType.NO_ORDER, "您已被禁止下单");
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getBuyerId(), buyerId)) {
            throw new RuntimeException("无权支付此订单");
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new RuntimeException(isReadyMadeOrder(order) ? "当前状态不可支付全款" : "当前状态不可支付定金");
        }
        if ("FROZEN".equals(order.getEscrowStatus())) {
            throw new RuntimeException("订单处于纠纷冻结中");
        }
        deductProductStock(order);
        boolean readyMade = isReadyMadeOrder(order);
        normalizeReadyMadePayment(order);
        double amount = readyMade
                ? round(order.getPrice())
                : (order.getDepositAmount() != null ? order.getDepositAmount() : round(order.getPrice() * DEPOSIT_RATIO));
        String channel = normalizePaymentChannel(paymentChannel);
        boolean useZaowuCoin = "ZAOWU_COIN".equals(channel);
        if (useZaowuCoin) {
            holdEscrow(order, buyerId, amount, readyMade ? "FULL_PAY" : "DEPOSIT",
                    readyMade ? "支付全款（造物币托管）" : "支付定金（造物币托管）");
            order.setEscrowStatus("HELD");
        } else {
            order.setEscrowStatus("NONE");
        }
        order.setStatus(readyMade ? "PENDING_SHIP" : "PRODUCING");
        String channelLabel = paymentChannelLabel(channel);
        CustomOrder saved = orderMapper.save(order);
        if (readyMade) {
            String milestoneNote = useZaowuCoin
                    ? "买家已支付全款 ￥" + amount + "（造物币托管）"
                    : "买家已通过" + channelLabel + "支付全款 ￥" + amount;
            addMilestoneRecord(orderId, "DEPOSIT", "支付全款", milestoneNote, null, buyerId, order.getBuyerName());
            notifyArtisan(saved, "买家已付款", "「" + safeTitle(order) + "」已全款支付，请尽快发货");
            notifyBuyer(saved, "支付成功", "「" + safeTitle(order) + "」已支付，等待卖家发货");
        } else {
            String milestoneNote = useZaowuCoin
                    ? "买家已支付定金 ￥" + amount + "（造物币托管）"
                    : "买家已通过" + channelLabel + "支付定金 ￥" + amount;
            addMilestoneRecord(orderId, "DEPOSIT", "支付定金", milestoneNote, null, buyerId, order.getBuyerName());
            notifyArtisan(saved, "买家已付定金", "「" + safeTitle(order) + "」进入制作阶段");
        }
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder confirmReceipt(Long orderId, Long buyerId) {
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getBuyerId(), buyerId)) {
            throw new RuntimeException("无权确认此订单");
        }
        if (!"PENDING_ACCEPT".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可确认收货");
        }
        if (!isReadyMadeOrder(order)) {
            throw new RuntimeException("定制订单请确认验收后支付尾款");
        }
        if ("FROZEN".equals(order.getEscrowStatus())) {
            throw new RuntimeException("订单处于纠纷冻结中");
        }
        releaseEscrowToArtisan(order);
        order.setStatus("COMPLETED");
        order.setEscrowStatus("RELEASED");
        order.setEscrowAmount(0.0);
        CustomOrder saved = orderMapper.save(order);
        incrementCompletedOrders(order.getBuyerId());
        incrementCompletedOrders(order.getArtisanId());
        addMilestoneRecord(orderId, "COMPLETE", "确认收货", "买家已确认收货，交易完成", null, buyerId, order.getBuyerName());
        notifyArtisan(saved, "订单已完成", "「" + safeTitle(order) + "」买家已确认收货，款项已结算");
        notifyBuyer(saved, "订单已完成", "「" + safeTitle(order) + "」交易完成，欢迎评价");
        coinEconomyService.grantEventReward(order.getArtisanId(), "order_complete", orderId, 10, "完成成品订单");
        coinEconomyService.grantEventReward(order.getBuyerId(), "order_complete_buyer", orderId, 5, "完成购买订单");
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder payBalance(Long orderId, Long buyerId) {
        userPunishmentService.assertNotPunished(buyerId, UserPunishmentType.NO_ORDER, "您已被禁止下单");
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getBuyerId(), buyerId)) {
            throw new RuntimeException("无权支付此订单");
        }
        if (!"PENDING_BALANCE".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可支付尾款");
        }
        if ("FROZEN".equals(order.getEscrowStatus())) {
            throw new RuntimeException("订单处于纠纷冻结中");
        }
        double amount = order.getBalanceAmount() != null ? order.getBalanceAmount() : round(order.getPrice() - order.getDepositAmount());
        holdEscrow(order, buyerId, amount, "BALANCE", "支付尾款（托管）");
        releaseEscrowToArtisan(order);
        order.setStatus("COMPLETED");
        order.setEscrowStatus("RELEASED");
        order.setEscrowAmount(0.0);
        CustomOrder saved = orderMapper.save(order);
        incrementCompletedOrders(order.getBuyerId());
        incrementCompletedOrders(order.getArtisanId());
        addMilestoneRecord(orderId, "BALANCE", "支付尾款", "买家已支付尾款 ￥" + amount + "，款项已结算给手作人", null, buyerId, order.getBuyerName());
        addMilestoneRecord(orderId, "COMPLETE", "订单完成", "交易完成，欢迎互相评价", null, buyerId, order.getBuyerName());
        notifyArtisan(saved, "订单已完成", "「" + safeTitle(order) + "」交易完成，款项已结算");
        notifyBuyer(saved, "订单已完成", "「" + safeTitle(order) + "」交易完成，欢迎评价");
        coinEconomyService.grantEventReward(order.getArtisanId(), "order_complete", orderId, 10, "完成定制订单");
        coinEconomyService.grantEventReward(order.getBuyerId(), "order_complete_buyer", orderId, 5, "完成购买订单");
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder updateStatus(Long orderId, String status, Long operatorId, String operatorName) {
        CustomOrder order = getOrder(orderId);
        if ("FROZEN".equals(order.getEscrowStatus()) && !"DISPUTED".equals(status)) {
            throw new RuntimeException("订单纠纷冻结中，无法变更状态");
        }
        validateTransition(order.getStatus(), status);
        order.setStatus(status);
        CustomOrder saved = orderMapper.save(order);
        String stageKey = mapStatusToStage(status);
        String label = statusLabel(status);
        addMilestoneRecord(orderId, stageKey, label, "订单进入「" + label + "」阶段", null, operatorId, operatorName);
        notifyOrderStatusChange(saved, operatorId, label);
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder shipOrder(Long orderId, Long artisanId, String shippingCompany, String trackingNumber, String operatorName) {
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getArtisanId(), artisanId)) {
            throw new RuntimeException("无权发货");
        }
        if (!"PENDING_SHIP".equals(order.getStatus())) {
            throw new RuntimeException("当前订单状态不可发货");
        }
        if (shippingCompany == null || shippingCompany.trim().isEmpty()) {
            throw new RuntimeException("请填写物流公司");
        }
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new RuntimeException("请填写快递单号");
        }
        applyShippingFromBuyer(order);
        requireOrderShippingAddress(order);
        order.setShippingCompany(shippingCompany.trim());
        order.setTrackingNumber(trackingNumber.trim());
        order.setShippedAt(LocalDateTime.now());
        order.setStatus("PENDING_ACCEPT");
        CustomOrder saved = orderMapper.save(order);
        String note = shippingCompany.trim() + " · 单号 " + trackingNumber.trim();
        addMilestoneRecord(orderId, "SHIP", "已发货", note, null, artisanId,
                operatorName != null ? operatorName : "手作人");
        notifyOrderStatusChange(saved, artisanId, "待收货");
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder addMilestone(Long orderId, Map<String, String> payload, Long operatorId, String operatorName) {
        CustomOrder order = getOrder(orderId);
        String stageKey = payload.getOrDefault("stageKey", "PRODUCING");
        String note = payload.getOrDefault("note", "");
        String imageUrl = payload.get("imageUrl");
        addMilestoneRecord(orderId, stageKey, "进度更新", note, imageUrl, operatorId, operatorName);
        return order;
    }

    @Override
    public List<OrderMilestone> getMilestones(Long orderId) {
        return milestoneMapper.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    @Override
    public List<EscrowTransaction> getEscrowTransactions(Long orderId) {
        return escrowMapper.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    @Override
    @Transactional
    public CustomOrder openDispute(Long orderId, Long userId, String reason) {
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getBuyerId(), userId) && !Objects.equals(order.getArtisanId(), userId)) {
            throw new RuntimeException("无权申请纠纷");
        }
        order.setStatus("DISPUTED");
        order.setEscrowStatus("FROZEN");
        CustomOrder saved = orderMapper.save(order);
        recordEscrow(orderId, userId, 0.0, "FREEZE", reason != null ? reason : "订单纠纷，资金冻结");
        addMilestoneRecord(orderId, "DISPUTE", "纠纷冻结", reason != null ? reason : "订单进入纠纷处理", null, userId, "用户");
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder resolveDispute(Long orderId, boolean releaseToArtisan) {
        CustomOrder order = getOrder(orderId);
        if (!"DISPUTED".equals(order.getStatus()) && !"FROZEN".equals(order.getEscrowStatus())) {
            throw new RuntimeException("订单未处于纠纷状态");
        }
        if (releaseToArtisan) {
            releaseEscrowToArtisan(order);
            order.setStatus("COMPLETED");
            order.setEscrowStatus("RELEASED");
            order.setEscrowAmount(0.0);
        } else {
            refundEscrowToBuyer(order);
            restoreProductStock(order);
            order.setStatus("CANCELLED");
            order.setEscrowStatus("NONE");
            order.setEscrowAmount(0.0);
        }
        return orderMapper.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        orderMapper.deleteById(id);
    }

    @Override
    @Transactional
    public CustomOrder requestCancel(Long orderId, Long buyerId, String reason) {
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getBuyerId(), buyerId)) {
            throw new RuntimeException("仅买家可申请取消订单");
        }
        if (!isCancellableStatus(order.getStatus())) {
            throw new RuntimeException("当前订单状态不可申请取消");
        }
        if ("PENDING".equals(normalizeCancelStatus(order.getCancelRequestStatus()))) {
            throw new RuntimeException("已有待卖家处理的取消申请");
        }
        order.setCancelRequestStatus("PENDING");
        order.setCancelReason(reason != null && !reason.isBlank() ? reason.trim() : null);
        CustomOrder saved = orderMapper.save(order);
        String note = order.getCancelReason() != null
                ? "买家申请取消：" + order.getCancelReason()
                : "买家申请取消订单，等待卖家确认";
        addMilestoneRecord(orderId, "CANCEL_REQUEST", "申请取消", note, null, buyerId, order.getBuyerName());
        notifyArtisan(saved, "买家申请取消订单", "买家申请取消「" + safeTitle(order) + "」，请尽快处理");
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder approveCancel(Long orderId, Long artisanId) {
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getArtisanId(), artisanId)) {
            throw new RuntimeException("无权处理此取消申请");
        }
        if (!"PENDING".equals(normalizeCancelStatus(order.getCancelRequestStatus()))) {
            throw new RuntimeException("没有待处理的取消申请");
        }
        User artisan = userMapper.findById(artisanId).orElse(null);
        String artisanName = artisan != null ? artisan.getUsername() : order.getArtisanName();
        finishCancelWithRefund(order, artisanId, artisanName, "卖家同意取消");
        CustomOrder saved = orderMapper.save(order);
        notifyBuyer(saved, "订单已取消", "卖家已同意取消「" + safeTitle(order) + "」，已退款至造物币钱包");
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder rejectCancel(Long orderId, Long artisanId, String reason) {
        CustomOrder order = getOrder(orderId);
        if (!Objects.equals(order.getArtisanId(), artisanId)) {
            throw new RuntimeException("无权处理此取消申请");
        }
        if (!"PENDING".equals(normalizeCancelStatus(order.getCancelRequestStatus()))) {
            throw new RuntimeException("没有待处理的取消申请");
        }
        order.setCancelRequestStatus("REJECTED");
        CustomOrder saved = orderMapper.save(order);
        User artisan = userMapper.findById(artisanId).orElse(null);
        String artisanName = artisan != null ? artisan.getUsername() : order.getArtisanName();
        String note = reason != null && !reason.isBlank()
                ? "卖家拒绝取消：" + reason.trim()
                : "卖家拒绝取消申请，订单继续履约";
        addMilestoneRecord(orderId, "CANCEL_REJECT", "拒绝取消", note, null, artisanId, artisanName);
        notifyBuyer(saved, "取消申请未通过", "卖家暂未同意取消「" + safeTitle(order) + "」");
        return saved;
    }

    private void finishCancelWithRefund(CustomOrder order, Long operatorId, String operatorName, String actionLabel) {
        double refundAmount = order.getEscrowAmount() != null ? order.getEscrowAmount() : 0.0;
        if (refundAmount > 0 && ("HELD".equals(order.getEscrowStatus()) || "FROZEN".equals(order.getEscrowStatus()))) {
            refundEscrowToBuyer(order, "取消订单退款给买家");
        }
        restoreProductStock(order);
        order.setStatus("CANCELLED");
        order.setEscrowStatus("NONE");
        order.setEscrowAmount(0.0);
        order.setCancelRequestStatus("NONE");
        order.setCancelReason(null);
        String note = refundAmount > 0
                ? actionLabel + "，已退款 ￥" + refundAmount + " 至买家造物币"
                : actionLabel + "，订单已关闭";
        addMilestoneRecord(order.getId(), "CANCEL", "订单取消", note, null, operatorId, operatorName);
    }

    private boolean isCancellableStatus(String status) {
        return status != null && !Set.of("COMPLETED", "CANCELLED", "DISPUTED").contains(status);
    }

    private String normalizeCancelStatus(String status) {
        return status == null || status.isBlank() ? "NONE" : status;
    }

    private void refundEscrowToBuyer(CustomOrder order) {
        refundEscrowToBuyer(order, "纠纷退款给买家");
    }

    private void refundEscrowToBuyer(CustomOrder order, String remark) {
        double total = order.getEscrowAmount() != null ? order.getEscrowAmount() : 0.0;
        if (total <= 0) return;
        User buyer = userMapper.findById(order.getBuyerId()).orElseThrow(() -> new RuntimeException("买家不存在"));
        double bal = buyer.getZaowuBiBalance() != null ? buyer.getZaowuBiBalance() : 0.0;
        buyer.setZaowuBiBalance(round(bal + total));
        userMapper.save(buyer);
        recordEscrow(order.getId(), order.getBuyerId(), total, "REFUND", remark);
    }

    private void holdEscrow(CustomOrder order, Long buyerId, double amount, String type, String remark) {
        User buyer = userMapper.findById(buyerId).orElseThrow(() -> new RuntimeException("用户不存在"));
        double balance = buyer.getZaowuBiBalance() != null ? buyer.getZaowuBiBalance() : 0.0;
        if (balance < amount) {
            throw new RuntimeException("造物币余额不足");
        }
        buyer.setZaowuBiBalance(round(balance - amount));
        userMapper.save(buyer);
        order.setEscrowAmount(round((order.getEscrowAmount() != null ? order.getEscrowAmount() : 0) + amount));
        recordEscrow(order.getId(), buyerId, amount, type, remark);
    }

    private void releaseEscrowToArtisan(CustomOrder order) {
        double total = order.getEscrowAmount() != null ? order.getEscrowAmount() : 0.0;
        if (total <= 0) return;
        User artisan = userMapper.findById(order.getArtisanId()).orElseThrow(() -> new RuntimeException("手作人不存在"));
        double bal = artisan.getZaowuBiBalance() != null ? artisan.getZaowuBiBalance() : 0.0;
        artisan.setZaowuBiBalance(round(bal + total));
        userMapper.save(artisan);
        recordEscrow(order.getId(), order.getArtisanId(), total, "RELEASE", "托管款释放给手作人");
    }

    private void recordEscrow(Long orderId, Long userId, double amount, String type, String remark) {
        EscrowTransaction tx = new EscrowTransaction();
        tx.setOrderId(orderId);
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setRemark(remark);
        escrowMapper.save(tx);
    }

    private void addMilestoneRecord(Long orderId, String stageKey, String stageLabel, String note, String imageUrl, Long operatorId, String operatorName) {
        OrderMilestone m = new OrderMilestone();
        m.setOrderId(orderId);
        m.setStageKey(stageKey);
        m.setStageLabel(stageLabel);
        m.setNote(note);
        m.setImageUrl(imageUrl);
        m.setOperatorId(operatorId);
        m.setOperatorName(operatorName);
        milestoneMapper.save(m);
    }

    private void incrementCompletedOrders(Long userId) {
        if (userId == null) return;
        userMapper.findById(userId).ifPresent(u -> {
            u.setCompletedOrders((u.getCompletedOrders() != null ? u.getCompletedOrders() : 0) + 1);
            u.setCreditScore((u.getCreditScore() != null ? u.getCreditScore() : 100) + 1);
            userMapper.save(u);
        });
    }

    private void validateTransition(String from, String to) {
        Set<String> allowed = transitionMap.getOrDefault(from, Collections.emptySet());
        if (!allowed.contains(to) && !from.equals(to)) {
            throw new RuntimeException("不允许从 " + from + " 变更为 " + to);
        }
    }

    private static final Map<String, Set<String>> transitionMap = new HashMap<>();
    static {
        transitionMap.put("PENDING_CONFIRM", Set.of("PENDING_PAY", "CANCELLED"));
        transitionMap.put("PENDING_PAY", Set.of("PRODUCING", "PENDING_SHIP", "CANCELLED"));
        transitionMap.put("PRODUCING", Set.of("HALF_FINISHED_CONFIRM", "PENDING_SHIP", "DISPUTED"));
        transitionMap.put("HALF_FINISHED_CONFIRM", Set.of("PRODUCING", "PENDING_SHIP", "DISPUTED"));
        transitionMap.put("PENDING_SHIP", Set.of("PENDING_ACCEPT", "RECEIVED", "DISPUTED"));
        transitionMap.put("PENDING_ACCEPT", Set.of("PENDING_BALANCE", "COMPLETED", "DISPUTED"));
        transitionMap.put("PENDING_BALANCE", Set.of("COMPLETED"));
        transitionMap.put("RECEIVED", Set.of("PENDING_BALANCE", "DISPUTED"));
        transitionMap.put("DISPUTED", Set.of("COMPLETED", "CANCELLED"));
    }

    private String mapStatusToStage(String status) {
        switch (status) {
            case "PENDING_CONFIRM": return "CONFIRM";
            case "PENDING_PAY": return "DEPOSIT";
            case "PRODUCING": return "PRODUCING";
            case "HALF_FINISHED_CONFIRM": return "PRODUCING";
            case "PENDING_SHIP": return "SHIP";
            case "PENDING_ACCEPT":
            case "RECEIVED": return "ACCEPT";
            case "PENDING_BALANCE": return "BALANCE";
            case "COMPLETED": return "COMPLETE";
            case "DISPUTED": return "DISPUTE";
            default: return "PRODUCING";
        }
    }

    private String statusLabel(String status) {
        switch (status) {
            case "PENDING_CONFIRM": return "待确认";
            case "PENDING_PAY": return "待付定金";
            case "PRODUCING": return "制作中";
            case "HALF_FINISHED_CONFIRM": return "半成品确认";
            case "PENDING_SHIP": return "待发货";
            case "PENDING_ACCEPT": return "待验收";
            case "PENDING_BALANCE": return "待付尾款";
            case "COMPLETED": return "已完成";
            case "DISPUTED": return "纠纷中";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String normalizePaymentChannel(String paymentChannel) {
        if (paymentChannel == null || paymentChannel.isBlank()) {
            return "ZAOWU_COIN";
        }
        return switch (paymentChannel.trim().toUpperCase()) {
            case "MOCK_WECHAT", "WECHAT" -> "MOCK_WECHAT";
            case "MOCK_ALIPAY", "ALIPAY" -> "MOCK_ALIPAY";
            default -> "ZAOWU_COIN";
        };
    }

    private String paymentChannelLabel(String channel) {
        return switch (channel) {
            case "MOCK_WECHAT" -> "微信";
            case "MOCK_ALIPAY" -> "支付宝";
            default -> "造物币";
        };
    }

    private int orderQuantity(CustomOrder order) {
        return order.getQuantity() != null && order.getQuantity() > 0 ? order.getQuantity() : 1;
    }

    private void validateProductStock(CustomOrder order) {
        if (order.getProductId() == null) return;
        Product product = productMapper.findById(order.getProductId()).orElse(null);
        if (product == null || !"READY_MADE".equals(product.getType())) return;
        int stock = product.getStock() != null ? product.getStock() : 0;
        int qty = orderQuantity(order);
        if (stock < qty) {
            throw new RuntimeException(stock <= 0 ? "商品已售罄" : "库存不足，当前仅剩 " + stock + " 件");
        }
    }

    private void deductProductStock(CustomOrder order) {
        if (order.getProductId() == null) return;
        Product product = productMapper.findById(order.getProductId()).orElse(null);
        if (product == null || !"READY_MADE".equals(product.getType())) return;
        int stock = product.getStock() != null ? product.getStock() : 0;
        int qty = orderQuantity(order);
        if (stock < qty) {
            throw new RuntimeException(stock <= 0 ? "商品已售罄" : "库存不足，当前仅剩 " + stock + " 件");
        }
        product.setStock(stock - qty);
        productMapper.save(product);
    }

    private void restoreProductStock(CustomOrder order) {
        if (order.getProductId() == null) return;
        Product product = productMapper.findById(order.getProductId()).orElse(null);
        if (product == null || !"READY_MADE".equals(product.getType())) return;
        int stock = product.getStock() != null ? product.getStock() : 0;
        product.setStock(stock + orderQuantity(order));
        productMapper.save(product);
    }

    private void notifyArtisan(CustomOrder order, String title, String content) {
        if (order.getArtisanId() == null) return;
        notificationService.notify(order.getArtisanId(), "ORDER", title, content, "/artisan-dashboard?menu=orders");
    }

    private void notifyBuyer(CustomOrder order, String title, String content) {
        if (order.getBuyerId() == null) return;
        notificationService.notify(order.getBuyerId(), "ORDER", title, content, "/artisan-dashboard?menu=buyer-orders");
    }

    private void notifyOrderStatusChange(CustomOrder order, Long operatorId, String label) {
        String content = "「" + safeTitle(order) + "」进入「" + label + "」阶段";
        if (Objects.equals(operatorId, order.getBuyerId())) {
            notifyArtisan(order, "订单状态更新", content);
        } else if (Objects.equals(operatorId, order.getArtisanId())) {
            notifyBuyer(order, "订单状态更新", content);
        }
    }

    private String safeTitle(CustomOrder order) {
        return order.getProductTitle() != null ? order.getProductTitle() : "定制订单";
    }

    private String safeName(String name) {
        return name != null ? name : "买家";
    }

    private String buildCustomRequirementsMilestoneNote(String requirements) {
        if (requirements == null || requirements.isBlank()) {
            return "订单已创建，等待手作人确认（买家未填写详细定制描述）";
        }
        return "买家定制需求：" + requirements.trim();
    }

    private String buildCustomOrderNotifyContent(CustomOrder order) {
        String content = "买家「" + safeName(order.getBuyerName()) + "」下单「" + safeTitle(order) + "」";
        if (order.getRequirements() != null && !order.getRequirements().isBlank()) {
            String preview = order.getRequirements().trim();
            if (preview.length() > 80) {
                preview = preview.substring(0, 80) + "...";
            }
            content += "，需求：" + preview;
        }
        return content;
    }

    private boolean isReadyMadeOrder(CustomOrder order) {
        if ("READY_MADE".equals(order.getProductType())) {
            return true;
        }
        if (order.getProductId() != null) {
            Product product = productMapper.findById(order.getProductId()).orElse(null);
            return product != null && "READY_MADE".equals(product.getType());
        }
        return false;
    }

    /** 修正历史成品订单仍按 30% 定金存储的问题，返回是否有变更 */
    private boolean normalizeReadyMadePayment(CustomOrder order) {
        if (!isReadyMadeOrder(order) || order.getPrice() == null) {
            return false;
        }
        if (order.getProductType() == null || order.getProductType().isBlank()) {
            order.setProductType("READY_MADE");
        }
        double full = round(order.getPrice());
        boolean changed = !Double.valueOf(1.0).equals(order.getDepositRatio())
                || order.getDepositAmount() == null
                || Math.abs(order.getDepositAmount() - full) > 0.001
                || order.getBalanceAmount() == null
                || order.getBalanceAmount() > 0.001;
        order.setDepositRatio(1.0);
        order.setDepositAmount(full);
        order.setBalanceAmount(0.0);
        return changed;
    }

    /** 成品订单不应停留在定制流程的中间状态 */
    private boolean normalizeReadyMadeStatus(CustomOrder order) {
        if (!isReadyMadeOrder(order)) {
            return false;
        }
        String status = order.getStatus();
        if (status == null) {
            return false;
        }
        boolean changed = false;
        switch (status) {
            case "PENDING_CONFIRM" -> {
                order.setStatus("PENDING_PAY");
                changed = true;
            }
            case "PRODUCING", "HALF_FINISHED_CONFIRM" -> {
                order.setStatus(hasPaidEscrow(order) ? "PENDING_SHIP" : "PENDING_PAY");
                changed = true;
            }
            case "PENDING_BALANCE" -> {
                order.setStatus("PENDING_ACCEPT");
                changed = true;
            }
            default -> { }
        }
        return changed;
    }

    private boolean hasPaidEscrow(CustomOrder order) {
        return "HELD".equals(order.getEscrowStatus())
                || "FROZEN".equals(order.getEscrowStatus())
                || (order.getEscrowAmount() != null && order.getEscrowAmount() > 0);
    }

    private CustomOrder normalizeOrderForRead(CustomOrder order) {
        boolean paymentChanged = normalizeReadyMadePayment(order);
        boolean statusChanged = normalizeReadyMadeStatus(order);
        if (paymentChanged || statusChanged) {
            return orderMapper.save(order);
        }
        return order;
    }

    private void applyShippingFromBuyer(CustomOrder order) {
        if (hasOrderShippingAddress(order) || order.getBuyerId() == null) {
            return;
        }
        userMapper.findById(order.getBuyerId()).ifPresent(buyer -> {
            if (order.getShippingName() == null || order.getShippingName().isBlank()) {
                order.setShippingName(buyer.getShippingName());
            }
            if (order.getShippingPhone() == null || order.getShippingPhone().isBlank()) {
                order.setShippingPhone(buyer.getShippingPhone());
            }
            if (order.getShippingAddress() == null || order.getShippingAddress().isBlank()) {
                order.setShippingAddress(buyer.getShippingAddress());
            }
        });
    }

    private void requireOrderShippingAddress(CustomOrder order) {
        if (!hasOrderShippingAddress(order)) {
            throw new RuntimeException("请先在个人信息中填写收货地址");
        }
    }

    private boolean hasOrderShippingAddress(CustomOrder order) {
        return order.getShippingName() != null && !order.getShippingName().isBlank()
                && order.getShippingPhone() != null && !order.getShippingPhone().isBlank()
                && order.getShippingAddress() != null && !order.getShippingAddress().isBlank();
    }
}
