package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Knowledge {

    private String name;

    private String description;

    /**
     * 索引模式（选填，建议填写）
     * high_quality 高质量
     * economy 经济
     */
    @JsonProperty("indexing_technique")
    private String indexingTechnique;

    private String permission;

    private String provider;

    @JsonProperty("external_knowledge_api_id")
    private String externalKnowledgeApiId;

    @JsonProperty("external_knowledge_id")
    private String externalKnowledgeId;
}
