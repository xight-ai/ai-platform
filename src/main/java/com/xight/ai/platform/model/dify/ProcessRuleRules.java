package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProcessRuleRules {
    @JsonProperty("pre_processing_rules")
    private List<PreProcessingRule> preProcessingRules;

    private ProcessRuleSegmentation segmentation;
}
