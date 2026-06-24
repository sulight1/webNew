package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订单实体，对应数据库表 custom_orders。
 * 涵盖成品直购与定制作品两类交易，含支付、托管、物流及取消流程。
 */
@Entity
@Data
@Table(name = "custom_orders")
public class CustomOrder {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 买家用户 ID */
    private Long buyerId;

    /** 买家昵称（快照） */
    private String buyerName;

    /** 手艺人用户 ID */
    private Long artisanId;

    /** 手艺人昵称（快照） */
    private String artisanName;

    /** 关联作品 ID */
    private Long productId;

    /** 来源定制需求 ID（从需求大厅选人合作时写入） */
    private Long customRequestId;

    /** 作品标题（快照） */
    private String productTitle;

    /** 作品类型：READY_MADE 或 CUSTOMIZABLE */
    private String productType;

    /** 订单总价（元） */
    private Double price;

    /** 购买数量（成品订单，默认 1；price 为总价 = 单价 × 数量） */
    private Integer quantity = 1;

    /** 定制需求说明 */
    private String requirements;

    /**
     * 订单状态。
     * 成品 READY_MADE：PENDING_PAY → PENDING_SHIP → PENDING_ACCEPT → COMPLETED；
     * 定制 CUSTOMIZABLE：PENDING_CONFIRM → PENDING_PAY → PRODUCING → … → PENDING_BALANCE → COMPLETED；
     * 另有 DISPUTED / CANCELLED。
     */
    private String status;

    /** 定金比例，定制默认 0.3，成品为 1.0 */
    private Double depositRatio = 0.3;

    /** 定金金额 */
    private Double depositAmount = 0.0;

    /** 尾款金额 */
    private Double balanceAmount = 0.0;

    /** 当前托管金额 */
    private Double escrowAmount = 0.0;

    /** 托管状态：NONE / HELD / RELEASED / FROZEN */
    private String escrowStatus = "NONE";

    /** 买家是否已评价 */
    private Boolean buyerReviewed = false;

    /** 手艺人是否已评价 */
    private Boolean artisanReviewed = false;

    /** 取消申请状态：NONE / PENDING / REJECTED */
    private String cancelRequestStatus = "NONE";

    /** 取消原因 */
    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    /** 收货人姓名（下单时快照） */
    private String shippingName;

    /** 收货人手机（下单时快照） */
    private String shippingPhone;

    /** 收货详细地址（下单时快照） */
    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    /** 物流公司 */
    private String shippingCompany;

    /** 快递单号 */
    private String trackingNumber;

    /** 发货时间 */
    private LocalDateTime shippedAt;

    /** 下单时间 */
    private LocalDateTime createTime;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = "READY_MADE".equals(productType) ? "PENDING_PAY" : "PENDING_CONFIRM";
        }
        applyPaymentSplit();
    }

    /**
     * JPA 更新前回调，自动刷新更新时间。
     */
    @PreUpdate
    protected void onUpdate() {
        applyPaymentSplit();
    }

    /** 按订单类型计算应付金额：成品全款，定制定金+尾款 */
    private void applyPaymentSplit() {
        if (price == null) return;
        if ("READY_MADE".equals(productType)) {
            depositRatio = 1.0;
            depositAmount = roundMoney(price);
            balanceAmount = 0.0;
            return;
        }
        if (depositRatio == null) {
            depositRatio = 0.3;
        }
        depositAmount = roundMoney(price * depositRatio);
        balanceAmount = roundMoney(price - depositAmount);
    }

    /**
     * 执行 roundMoney 相关逻辑。
     */
    private double roundMoney(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
