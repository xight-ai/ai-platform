package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WikiSpace {

    private String description;

    private String name;

    @JsonProperty(value = "space_id")
    private String spaceId;

    // 表示知识空间类型 team：团队空间 person：个人空间
    @JsonProperty(value = "space_type")
    private String spaceType;

    // 表示知识空间可见性 public：公开空间 private：私有空间
    private String visibility;

    // 表示知识空间的分享状态 open：打开 closed：关闭
    @JsonProperty("open_sharing")
    private String openSharing;
}
