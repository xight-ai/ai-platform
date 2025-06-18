package com.xight.ai.platform.model.feishu;

import lombok.Data;

import java.util.List;

@Data
public class WikiCache {
    private WikiSpace wikiSpace;

    private List<WikiSpaceMember> members;
}
