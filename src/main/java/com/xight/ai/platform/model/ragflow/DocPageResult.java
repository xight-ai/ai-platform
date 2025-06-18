package com.xight.ai.platform.model.ragflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocPageResult {
    private List<RagflowDocument> docs;

    private int total;
}
