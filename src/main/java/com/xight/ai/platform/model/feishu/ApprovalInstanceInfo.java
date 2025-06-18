package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ApprovalInstanceInfo {
    @JsonProperty("approval_code")
    private String approvalCode;

    @JsonProperty("approval_name")
    private String approvalName;

    @JsonProperty("department_id")
    private String departmentId;

    @JsonProperty("end_time")
    private String endTime;

    private String form;

    @JsonProperty("instance_code")
    private String instanceCode;

    @JsonProperty("open_id")
    private String openId;

    private String reverted;

    @JsonProperty("serial_number")
    private String serialNumber;

    @JsonProperty("start_time")
    private String startTime;

    private String status;

    @JsonProperty("task_list")
    private List<ApprovalTask> taskList;

    private List<ApprovalTimeline> timeline;

    @JsonProperty("user_id")
    private String userId;

    private String uuid;
}
