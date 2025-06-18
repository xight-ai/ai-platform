package com.xight.ai.platform.model.ragflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrieveResult {
    private Integer code;

    private RetrieveData data;
}
