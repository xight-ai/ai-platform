package com.xight.ai.platform.model.feishu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExportTaskInfo {

    /**
     * 将云文档导出为本地文件后，本地文件的扩展名
     */
    @JsonProperty("file_extension")
    private String fileExtension;

    /**
     * 要导出的文档类型
     */
    private String type;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_token")
    private String fileToken;

    @JsonProperty("file_size")
    private String fileSize;

    @JsonProperty("job_error_msg")
    private String jobErrorMsg;

    @JsonProperty("job_status")
    private Integer jobStatus;

    @JsonIgnore
    private String extra;
}