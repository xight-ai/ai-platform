package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FeishuUserInfo {

    private String name;

    @JsonProperty("en_name")
    private String enName;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("avatar_thumb")
    private String avatarThumb;

    @JsonProperty("avatar_middle")
    private String avatarMiddle;

    @JsonProperty("avatar_big")
    private String avatarBig;

    @JsonProperty("open_id")
    private String openId;

    @JsonProperty("union_id")
    private String unionId;

    private String email;

    @JsonProperty("enterprise_email")
    private String enterpriseEmail;

    @JsonProperty("user_id")
    private String userId;

    private String mobile;

    @JsonProperty("tenant_key")
    private String tenantKey;

    @JsonProperty("employee_no")
    private String employeeNo;
}
