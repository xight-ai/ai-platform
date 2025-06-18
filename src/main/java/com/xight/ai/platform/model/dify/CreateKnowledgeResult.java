package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateKnowledgeResult {
    private String id;

    private String name;

    private String description;

    private String provider;

    private String permission;

    @JsonProperty("data_source_type")
    private String dataSourceType;

    @JsonProperty("indexing_technique")
    private String indexingTechnique;

    @JsonProperty("app_count")
    private Integer appCount;

    @JsonProperty("document_count")
    private Integer documentCount;

    @JsonProperty("word_count")
    private Integer wordCount;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("updated_at")
    private Long updatedAt;

    @JsonProperty("embedding_model")
    private String embeddingModel;

    @JsonProperty("embedding_model_provider")
    private String embeddingModelProvider;

    @JsonProperty("embedding_available")
    private String embeddingAvailable;
}
