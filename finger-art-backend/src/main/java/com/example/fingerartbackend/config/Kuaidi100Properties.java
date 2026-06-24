package com.example.fingerartbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 快递100物流查询配置属性，绑定 {@code logistics.kuaidi100.*} 配置项。
 * <p>
 * 控制第三方物流轨迹查询功能的开关及 API 凭证。
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "logistics.kuaidi100")
@Data
public class Kuaidi100Properties {

    /** 是否启用快递100物流查询 */
    private boolean enabled = false;

    /** 快递100 授权 customer 参数 */
    private String customer = "";

    /** 快递100 API Key */
    private String key = "";

    /** 物流轨迹查询接口地址 */
    private String queryUrl = "https://poll.kuaidi100.com/poll/query.do";
}
