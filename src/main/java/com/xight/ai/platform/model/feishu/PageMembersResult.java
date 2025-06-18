package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PageMembersResult {

    List<WikiSpaceMember> members;

    @JsonProperty(value = "page_token")
    private String pageToken;

    @JsonProperty(value = "has_more")
    private boolean hasMore;
}
