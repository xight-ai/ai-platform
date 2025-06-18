package com.xight.ai.platform.model.feishu;

import lombok.Data;

@Data
public class ResponseData<T> {
    private Integer code;

    private String msg;

    private T data;
}
