package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WikiNodeItem {

    @JsonProperty(value = "space_id")
    private String spaceId;

    @JsonProperty(value = "node_token")
    private String nodeToken;

    @JsonProperty(value = "obj_token")
    private String objToken;

    /**
     * 文档类型
     *
     * 可选值有：
     *
     * doc：旧版文档
     * sheet：表格
     * mindnote：思维导图
     * bitable：多维表格
     * file：文件
     * docx：新版文档
     * slides：幻灯片
     */
    @JsonProperty(value = "obj_type")
    private String objType;

    @JsonProperty(value = "parent_node_token")
    private String parentNodeToken;

    @JsonProperty(value = "node_type")
    private String nodeType;

    @JsonProperty(value = "origin_node_token")
    private String originNodeToken;

    @JsonProperty(value = "origin_space_id")
    private String originSpaceId;

    @JsonProperty(value = "has_child")
    private boolean hasChild;

    private String title;

    @JsonProperty(value = "obj_create_time")
    private String objCreateTime;

    @JsonProperty(value = "obj_edit_time")
    private String objEditTime;

    @JsonProperty(value = "node_create_time")
    private String nodeCreateTime;

    private String creator;

    private String owner;
}
