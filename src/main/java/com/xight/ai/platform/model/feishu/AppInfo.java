package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppInfo {

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("app_secret")
    private String appSecret;
}
