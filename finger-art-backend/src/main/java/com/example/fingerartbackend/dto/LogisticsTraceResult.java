package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LogisticsTraceResult {
    private String shippingCompany;
    private String trackingNumber;
    private String companyCode;
    /** 是否在平台内成功拉取轨迹 */
    private boolean apiAvailable;
    /** 物流状态文案（在途/签收等） */
    private String statusText;
    private List<LogisticsTrackItem> tracks = new ArrayList<>();
    /** 快递100 等外链查询地址（轻量降级） */
    private String fallbackUrl;
    private String hint;
}
