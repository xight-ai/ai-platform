package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WikiSpaceMember {

    @JsonProperty("member_id")
    private String memberId;

    @JsonProperty("member_role")
    private String memberRole;

    @JsonProperty("member_type")
    private String memberType;
}
