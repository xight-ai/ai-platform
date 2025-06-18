package com.xight.ai.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xight.ai.platform.model.ragflow.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagFlowHttpApi {
    @Value("${ragflow.baseUrl}")
    private String baseUrl;

    @Value("${ragflow.apiKey}")
    private String apiKey;

    @Value("${ragflow.pageSize}")
    private int pageSize;

    @Value("${ragflow.retrieveChunkTopN}")
    private int retrieveChunkTopN;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpHeaders generateAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        return headers;
    }

    /**
     * 检索ragflow知识库
     *
     * @param question   提问
     * @param datasetIds ragflow知识库的id
     * @return 分段
     */
    public List<Chunk> retrieveChunks(String question, List<String> datasetIds) {
        String url = baseUrl + "/api/v1/retrieval";

        Map<String, Object> params = new HashMap<>();
        params.put("question", question);
        params.put("dataset_ids", datasetIds);
        // 最多3个
        params.put("page_size", retrieveChunkTopN);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, generateAuthHeader());

        ResponseEntity<RetrieveResult> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, RetrieveResult.class);
        if (response.getBody() != null && response.getBody().getData() != null)
            return response.getBody().getData().getChunks();
        else
            return new ArrayList<>();
    }

    /**
     * 查询ragflow知识库列表
     *
     * @return 知识库列表
     */
    public List<Dataset> listDatasets(String datasetId, String name) {
        String url = baseUrl + "/api/v1/datasets?page_size=" + pageSize;
        if (datasetId != null) {
            url = url + "&id=" + datasetId;
        }
        if (name != null) {
            url = url + "&name=" + name;
        }
        HttpEntity<String> entity = new HttpEntity<>(generateAuthHeader());
        ResponseEntity<DataSetsResult> response = restTemplate.exchange(url, HttpMethod.GET, entity, DataSetsResult.class);
        if (response.getBody() != null)
            return response.getBody().getData();
        else
            return new ArrayList<>();
    }

    /**
     * 创建知识库
     *
     * @param datasetName 知识库名字
     * @return 创建结果
     */
    public CreateDatasetResult createDataSet(String datasetName) {
        String url = baseUrl + "/api/v1/datasets";

        // 使用 ObjectMapper 创建 ObjectNode，而不是直接使用 JsonNodeFactory
        ObjectNode paramNode = objectMapper.createObjectNode();
        paramNode.put("name", datasetName);

        ObjectNode parseConfigNode = objectMapper.createObjectNode();
        parseConfigNode.put("chunk_token_num", 1024);
        paramNode.set("parser_config", parseConfigNode);

        // 设置请求头，明确指定 JSON 格式和 UTF-8 编码
        HttpHeaders headers = generateAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // 使用 ObjectMapper 序列化 ObjectNode，确保使用 UTF-8 编码
            String jsonBody = objectMapper.writeValueAsString(paramNode);
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);

            // 发送请求
            ResponseEntity<CreateDatasetResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    CreateDatasetResult.class
            );

            return response.getBody();
        } catch (JsonProcessingException e) {
            // 处理序列化异常
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    /**
     * 删除知识库
     *
     * @param datasetIds 要删除的知识库的id列表
     * @return 删除结果
     */
    public RagFlowResult deleteDataSet(List<String> datasetIds) {
        String url = baseUrl + "/api/v1/datasets/";
        Map<String, Object> params = new HashMap<>();
        params.put("ids", datasetIds);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, generateAuthHeader());
        ResponseEntity<RagFlowResult> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, RagFlowResult.class);
        return response.getBody();
    }

    /**
     * 上传文档
     *
     * @param datasetId 知识库id
     * @param filePath  文档路径
     * @return 上传结果
     */
    public UploadDocResult uploadDocuments(String datasetId, String filePath) {
        String url = baseUrl + "/api/v1/datasets/" + datasetId + "/documents";
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        File file = new File(filePath);
        Resource fileResource = new FileSystemResource(file);
        form.add("file", fileResource);

        HttpHeaders headers = generateAuthHeader();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(form, headers);
        ResponseEntity<UploadDocResult> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, UploadDocResult.class);
        return response.getBody();
    }

    /**
     * 查询文档
     *
     * @param datasetId 知识库id
     * @param page      页码
     * @param docId     文档id
     * @param name      文档名
     * @return 查询出的知识库文档
     */
    public RagFlowDocResult listDocuments(String datasetId, Integer page, String docId, String name) {
        String url = baseUrl + "/api/v1/datasets/" + datasetId + "/documents?page_size=" + pageSize;
        if (page != null) {
            url = url + "&page=" + page;
        }
        if (docId != null) {
            url = url + "&id=" + docId;
        }
        if (name != null) {
            url = url + "&name=" + name;
        }
        HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());
        ResponseEntity<RagFlowDocResult> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, RagFlowDocResult.class);
        return response.getBody();
    }

    /**
     * 删除文档
     *
     * @param datasetId   知识库id
     * @param documentIds 要删除的文档id
     * @return 删除结果
     */
    public RagFlowResult deleteDocument(String datasetId, List<String> documentIds) {
        String url = baseUrl + "/api/v1/datasets/" + datasetId + "/documents";
        Map<String, Object> params = new HashMap<>();
        params.put("ids", documentIds);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, generateAuthHeader());
        ResponseEntity<RagFlowResult> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, RagFlowResult.class);
        return response.getBody();
    }

    /**
     * 解析文档
     *
     * @param datasetId   知识库id
     * @param documentIds 文档id
     * @return 解析结果
     */
    public RagFlowResult parseDocument(String datasetId, List<String> documentIds) {
        String url = baseUrl + "/api/v1/datasets/" + datasetId + "/chunks";
        Map<String, Object> params = new HashMap<>();
        params.put("document_ids", documentIds);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, generateAuthHeader());
        ResponseEntity<RagFlowResult> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, RagFlowResult.class);
        return response.getBody();
    }

}
