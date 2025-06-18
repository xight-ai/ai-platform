package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {
    private String id;

    private Integer position;

    @JsonProperty("data_source_type")
    private String dataSourceType;

    @JsonProperty("data_source_info")
    private DataSourceInfo dataSourceInfo;

    @JsonProperty("dataset_process_rule_id")
    private String datasetProcessRuleId;

    private String name;

    @JsonProperty("created_from")
    private String createdFrom;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private Long createdAt;

    private Integer tokens;

    @JsonProperty("indexing_status")
    private String indexingStatus;

    private String error;

    private boolean enabled;

    @JsonProperty("disabled_at")
    private Long disabledAt;

    @JsonProperty("disabled_by")
    private String disableBy;

    private boolean archived;

    @JsonProperty("display_status")
    private String displayStatus;

    @JsonProperty("word_count")
    private Integer wordCount;

    @JsonProperty("hit_count")
    private Integer hitCount;

    @JsonProperty("doc_form")
    private String docForm;
}