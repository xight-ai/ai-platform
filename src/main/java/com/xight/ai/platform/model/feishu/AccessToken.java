package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccessToken {
    // 错误码，非 0 取值表示失败
    private Integer code;

    // 错误描述
    private String msg;

    // 访问凭证
    @JsonProperty(value =  "tenant_access_token")
    private String tenantAccessToken;

    // token的过期时间，单位为秒
    private Integer expire;
}
