package com.xight.ai.platform.model.feishu;

import lombok.Data;

import java.util.List;

@Data
public class ApprovalSaved {

    private List<String> openIds;

    private Long timestamp;

    private String expireTime;
}
