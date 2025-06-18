package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DifyPageResult<T> {
    private List<T> data;

    @JsonProperty("has_more")
    private boolean hasMore;

    private Integer total;

    private Integer limit;

    private Integer page;
}
