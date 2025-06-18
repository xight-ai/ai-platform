package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RerankingModel {

    @JsonProperty("reranking_provider_name")
    private String rerankingProviderName;

    @JsonProperty("reranking_model_name")
    private String rerankingModelName;
}
