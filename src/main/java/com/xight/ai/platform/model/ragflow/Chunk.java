package com.xight.ai.platform.model.ragflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Chunk {
    private String content;

    @JsonProperty("content_ltks")
    private String contentLtks;

    @JsonProperty("dataset_id")
    private String datasetId;

    @JsonProperty("document_id")
    private String documentId;

    @JsonProperty("document_keyword")
    private String documentKeyword;

    private String highlight;

    private String id;

    @JsonProperty("image_id")
    private String imageId;

    private double similarity;

    @JsonProperty("term_similarity")
    private String termSimilarity;

    @JsonProperty("vector_similarity")
    private String vectorSimilarity;
}
