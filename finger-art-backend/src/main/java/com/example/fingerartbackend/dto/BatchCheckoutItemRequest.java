package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * 批量结算单项 DTO。
 * 表示购物车中某一作品及其购买数量。
 */
@Data
public class BatchCheckoutItemRequest {
    /** 作品 ID */
    private Long productId;

    /** 购买数量，默认 1 */
    private Integer quantity = 1;
}
