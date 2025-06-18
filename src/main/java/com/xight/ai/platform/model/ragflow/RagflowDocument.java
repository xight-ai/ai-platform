package com.xight.ai.platform.model.ragflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RagflowDocument {
    private String id;

    private String name;

    @JsonProperty(value = "create_time")
    private Long createTime;
}
