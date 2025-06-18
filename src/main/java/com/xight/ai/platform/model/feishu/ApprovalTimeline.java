package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApprovalTimeline {
    @JsonProperty("create_time")
    private String createTime;

    private String ext;

    @JsonProperty("node_key")
    private String nodeKey;

    @JsonProperty("open_id")
    private String openId;

    private String type;

    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("user_id")
    private String userId;
}
