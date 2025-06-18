package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApprovalEvent {
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("approval_code")
    private String approvalCode;

    @JsonProperty("instance_code")
    private String instanceCode;

    @JsonProperty("instance_operate_time")
    private String instanceOperateTime;

    @JsonProperty("operate_time")
    private String operateTime;

    private String status;

    @JsonProperty("tenant_key")
    private String tenantKey;

    private String type;

    private String uuid;
}
