package com.xight.ai.platform.model.ragflow;

import lombok.Data;

import java.util.List;

@Data
public class DataSetsResult {
    private int code;

    private List<Dataset> data;
}
