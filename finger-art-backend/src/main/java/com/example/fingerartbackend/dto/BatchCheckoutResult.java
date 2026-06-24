package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.CustomOrder;
import lombok.Data;

import java.util.List;

/**
 * 批量结算结果 DTO。
 * 返回本次创建的订单列表及汇总金额。
 */
@Data
public class BatchCheckoutResult {
    /** 本次创建的订单列表 */
    private List<CustomOrder> orders;

    /** 订单总金额（元） */
    private Double totalAmount;

    /** 订单数量 */
    private Integer orderCount;
}
