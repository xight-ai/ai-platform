package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentUser {
    @JsonProperty("mobile_visible")
    private boolean mobileVisible;

    @JsonProperty("open_id")
    private String openId;

    @JsonProperty("union_id")
    private String unionId;

    @JsonProperty("user_id")
    private String userId;
}
