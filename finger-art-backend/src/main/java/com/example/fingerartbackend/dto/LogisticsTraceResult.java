package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 物流轨迹查询结果 DTO。
 * 用于订单详情页展示快递公司与轨迹节点。
 */
@Data
public class LogisticsTraceResult {
    /** 物流公司名称 */
    private String shippingCompany;

    /** 快递单号 */
    private String trackingNumber;

    /** 物流公司编码（对接第三方 API 用） */
    private String companyCode;

    /** 是否在平台内成功拉取轨迹 */
    private boolean apiAvailable;

    /** 物流状态文案（在途/签收等） */
    private String statusText;

    /** 轨迹节点列表，按时间倒序 */
    private List<LogisticsTrackItem> tracks = new ArrayList<>();

    /** 快递100 等外链查询地址（API 不可用时的降级方案） */
    private String fallbackUrl;

    /** 给用户的提示说明 */
    private String hint;
}
