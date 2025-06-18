package com.xight.ai.platform.model.dify;

import lombok.Data;

import java.util.List;

@Data
public class QueryResult {

    private QueryContent query;

    List<QueryRecord> records;
}
