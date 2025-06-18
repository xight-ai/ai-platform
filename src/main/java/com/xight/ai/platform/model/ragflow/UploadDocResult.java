package com.xight.ai.platform.model.ragflow;

import lombok.Data;

import java.util.List;

@Data
public class UploadDocResult {
    private Integer code;

    private List<RagflowDocument> data;
}
