package com.xight.ai.platform.model.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataSourceInfo {

    @JsonProperty("upload_file_id")
    private String uploadFileId;
}
