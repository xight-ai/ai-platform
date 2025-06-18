package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProcessRuleSegmentation {

    private String separator;

    @JsonProperty("max_tokens")
    private Integer maxTokens;
}
