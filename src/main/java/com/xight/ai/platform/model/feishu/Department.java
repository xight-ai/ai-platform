package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Department {

    private String name;

    @JsonProperty("open_department_id")
    private String openDepartmentId;
}
