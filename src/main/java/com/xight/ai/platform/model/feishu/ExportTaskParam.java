package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExportTaskParam {

    @JsonProperty("file_extension")
    private String fileExtension;

    private String token;

    private String type;
}
