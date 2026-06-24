package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量结算下单请求 DTO。
 * 用于购物车多件作品一次性创建订单并支付。
 */
@Data
public class BatchCheckoutRequest {
    /** 买家用户 ID */
    private Long buyerId;

    /** 买家昵称 */
    private String buyerName;

    /** 支付渠道，默认 ZAOWU_COIN（造物币） */
    private String paymentChannel = "ZAOWU_COIN";

    /** 待结算商品列表 */
    private List<BatchCheckoutItemRequest> items;
}
