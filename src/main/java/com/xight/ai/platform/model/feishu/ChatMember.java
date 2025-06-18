package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatMember {
    @JsonProperty("member_id_type")
    private String memberIdType;

    @JsonProperty("member_id")
    private String memberId;

    private String name;

    @JsonProperty("tenant_key")
    private String tenantKey;
}
