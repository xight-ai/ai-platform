package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApprovalTask {

    @JsonProperty("end_time")
    private String endTime;

    private String id;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("node_name")
    private String nodeName;

    @JsonProperty("open_id")
    private String openId;

    @JsonProperty("start_time")
    private String startTime;

    private String status;

    private String type;

    @JsonProperty("user_id")
    private String userId;
}
