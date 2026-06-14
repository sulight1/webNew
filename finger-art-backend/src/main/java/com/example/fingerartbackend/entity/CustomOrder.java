package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "custom_orders")
public class CustomOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long buyerId;
    private String buyerName;
    private Long artisanId;
    private String artisanName;
    private Long productId;

    /** 来源定制需求 ID（从需求大厅选人合作时写入） */
    private Long customRequestId;

    private String productTitle;
    private String productType;
    private Double price;
    private String requirements;

    /**
     * 成品 READY_MADE: PENDING_PAY(全款) → PENDING_SHIP → PENDING_ACCEPT → COMPLETED
     * 定制 CUSTOMIZABLE: PENDING_CONFIRM → PENDING_PAY(定金) → PRODUCING → ... → PENDING_BALANCE → COMPLETED
     * DISPUTED / CANCELLED
     */
    private String status;

    private Double depositRatio = 0.3;
    private Double depositAmount = 0.0;
    private Double balanceAmount = 0.0;
    private Double escrowAmount = 0.0;

    /** NONE / HELD / RELEASED / FROZEN */
    private String escrowStatus = "NONE";

    private Boolean buyerReviewed = false;
    private Boolean artisanReviewed = false;

    /** 取消申请：NONE / PENDING / REJECTED */
    private String cancelRequestStatus = "NONE";

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = "READY_MADE".equals(productType) ? "PENDING_PAY" : "PENDING_CONFIRM";
        }
        applyPaymentSplit();
    }

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

    private double roundMoney(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
