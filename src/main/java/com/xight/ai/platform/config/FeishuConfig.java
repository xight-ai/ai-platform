package com.xight.ai.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "feishu")
public class FeishuConfig {
    private String appId;

    private String appSecret;

    private String exportPath;

    private String docSaveType;

    private String openApiEndPoint;

    private String redirectUri;

    private String authorizeUrl;
}
