package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DocumentData {

    /**
     * 索引方式
     *
     * high_quality 高质量：使用 embedding 模型进行嵌入，构建为向量数据库索引
     * economy 经济：使用 keyword table index 的倒排索引进行构建
     */
    @JsonProperty("indexing_technique")
    private String indexingTechnique;

    /**
     * 处理规则
     */
    @JsonProperty("process_rule")
    private ProcessRule processRule;
}
