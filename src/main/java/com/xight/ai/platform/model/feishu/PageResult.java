package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResult<T> {
    private List<T> items;

    @JsonProperty(value = "page_token")
    private String pageToken;

    @JsonProperty(value = "has_more")
    private boolean hasMore;
}
