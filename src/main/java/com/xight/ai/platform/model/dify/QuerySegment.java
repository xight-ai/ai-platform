package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class QuerySegment {

    private String id;

    private Integer position;

    @JsonProperty("document_id")
    private String documentId;

    private String content;

    private String answer;

    @JsonProperty("word_count")
    private Integer wordCount;

    private Integer tokens;

    private List<String> keywords;

    @JsonProperty("index_node_id")
    private String indexNodeId;

    @JsonProperty("index_node_hash")
    private String indexNodeHash;

    @JsonProperty("hit_count")
    private Integer hitCount;

    private boolean enabled;

    @JsonProperty("disabled_at")
    private Long disabledAt;

    @JsonProperty("disabled_by")
    private String disabledBy;

    private String status;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("indexing_at")
    private Long indexingAt;

    @JsonProperty("completed_at")
    private Long completedAt;

    private String error;

    @JsonProperty("stopped_at")
    private Long stoppedAt;

    private QueryDocument document;
}
