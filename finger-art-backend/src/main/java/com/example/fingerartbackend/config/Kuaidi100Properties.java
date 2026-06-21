package com.example.fingerartbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "logistics.kuaidi100")
@Data
public class Kuaidi100Properties {
    private boolean enabled = false;
    private String customer = "";
    private String key = "";
    private String queryUrl = "https://poll.kuaidi100.com/poll/query.do";
}
