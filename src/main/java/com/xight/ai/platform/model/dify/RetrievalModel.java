package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrievalModel {

    @JsonProperty("search_method")
    private String searchMethod;

    @JsonProperty("reranking_enable")
    private boolean rerankingEnable;

    @JsonProperty("reranking_mode")
    private String rerankingMode;

    @JsonProperty("reranking_model")
    private RerankingModel rerankingModel;

//    private String weights;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("score_threshold_enabled")
    private boolean scoreThresholdEnabled;

    @JsonProperty("score_threshold")
    private String scoreThreshold;
}
