package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class QueryRecord {

    private QuerySegment segment;

    private Double score;

    @JsonProperty("tsne_position")
    private String tsnePosition;

    @JsonProperty("child_chunks")
    private String childChunks;
}
