package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class QueryDocument {
    private String id;

    @JsonProperty("data_source_type")
    private String dataSourceType;

    private String name;

    @JsonProperty("doc_type")
    private String docType;
}
