package com.xight.ai.platform.model.feishu;

import lombok.Data;

@Data
public class EventCallback {

    private String uuid;

    private ApprovalEvent event;

    private String token;

    private String ts;

    private String type;
}
