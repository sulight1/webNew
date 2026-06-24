package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.CustomRequest;
import lombok.Data;

/**
 * 选中竞标结果 DTO。
 * 买家选定手艺人后返回生成的订单及更新后的需求状态。
 */
@Data
public class SelectBidResult {
    /** 新创建的定制订单 */
    private CustomOrder order;

    /** 更新后的定制需求 */
    private CustomRequest request;
}
