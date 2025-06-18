package com.xight.ai.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xight.ai.platform.model.dify.*;
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

@Service
public class DifyHttpApi {

    @Value("${dify.apikey}")
    private String apiKey;

    @Value("${dify.baseUrl}")
    private String baseUrl;

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
     * 创建空知识库
     *
     * @param knowledge 知识库参数
     * @return 创建结果
     */
    public CreateKnowledgeResult createEmptyKnowledge(Knowledge knowledge) {
        String url = baseUrl + "/v1/datasets";

        HttpEntity<Knowledge> entity = new HttpEntity<>(knowledge, generateAuthHeader());

        ResponseEntity<CreateKnowledgeResult> response = restTemplate.exchange(url, HttpMethod.POST, entity, CreateKnowledgeResult.class);
        return response.getBody();
    }

    /**
     * 获取知识库列表
     *
     * @param page 页码，从1开始
     * @return 一页知识库
     */
    public DifyPageResult<KnowLedgeInfo> getKnowledgeList(Integer page) {
        try {
            String url = baseUrl + "/v1/datasets?page={page}&limit=100";

            HttpEntity<String> entity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, page);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<DifyPageResult<KnowLedgeInfo>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检索知识库
     *
     * @param knowledgeId 知识库id
     * @param query       用户的问题
     * @return 检索结果
     */
    public QueryResult queryKnowledge(String knowledgeId, String query) {
        String url = baseUrl + "/v1/datasets/" + knowledgeId + "/retrieve";

        Query queryObject = new Query();
        queryObject.setQuery(query);

        HttpEntity<Query> entity = new HttpEntity<>(queryObject, generateAuthHeader());

        ResponseEntity<QueryResult> response = restTemplate.exchange(url, HttpMethod.POST, entity, QueryResult.class);
        return response.getBody();
    }

    /**
     * 删除知识库
     *
     * @param id 知识库id
     * @return 删除结果
     */
    public String deleteKnowledge(String id) {
        String url = baseUrl + "/v1/datasets/{id}";

        HttpEntity<String> entity = new HttpEntity<>(generateAuthHeader());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class, id);
        return response.getBody();
    }

    /**
     * 通过文件创建文档
     *
     * @param knowledgeId  知识库id
     * @param documentData 创建文档的配置信息
     * @param filePath     文档文件路径
     * @return 创建的文档信息
     */
    public DocumentInfo createDocByFile(String knowledgeId, DocumentData documentData, String filePath) {
        System.out.println(knowledgeId + "***");
        String url = baseUrl + "/v1/datasets/" + knowledgeId + "/document/create-by-file";

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        try {
            form.add("data", objectMapper.writeValueAsString(documentData));
            System.out.println(objectMapper.writeValueAsString(documentData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        File file = new File(filePath);
        Resource fileResource = new FileSystemResource(file);
        form.add("file", fileResource);

        HttpHeaders headers = generateAuthHeader();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<DocumentInfo> response = restTemplate.exchange(url, HttpMethod.POST, entity, DocumentInfo.class);
        return response.getBody();
    }

    /**
     * 知识库文档列表
     *
     * @param knowledgeId 知识库id
     * @param page        页码
     * @return 一页文档
     */
    public DifyPageResult<Document> getDocumentList(String knowledgeId, Integer page) {
        try {
            // limit直接设置个默认100
            String url = baseUrl + "/v1/datasets/" + knowledgeId + "/documents?page={page}&limit=100";

            HttpEntity<String> entity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, page);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<DifyPageResult<Document>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过文件更新文档
     *
     * @param knowledgeId  知识库id
     * @param documentId   要更新的文档id
     * @param documentData 要更新的文档配置数据
     * @param filePath     要更新的文档文件路径
     * @return 更新后的文档信息
     */
    public DocumentInfo updateDocByFile(String knowledgeId, String documentId, DocumentData documentData, String filePath) {
        String url = baseUrl + "/v1/datasets/" + knowledgeId + "/documents/" + documentId + "/update-by-file";

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        if (documentData != null) {
            try {
                System.out.println("has data!");
                form.add("data", objectMapper.writeValueAsString(documentData));
                System.out.println(objectMapper.writeValueAsString(documentData));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        File file = new File(filePath);
        Resource fileResource = new FileSystemResource(file);
        form.add("file", fileResource);

        HttpHeaders headers = generateAuthHeader();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<DocumentInfo> response = restTemplate.exchange(url, HttpMethod.POST, entity, DocumentInfo.class);
        System.out.println(response.getBody());
        return response.getBody();
    }

    /**
     * 删除文档
     *
     * @param knowledgeId 知识库id
     * @param documentId  文档id
     * @return 删除是否成功
     */
    public String deleteDoc(String knowledgeId, String documentId) {
        String url = baseUrl + "/v1/datasets/" + knowledgeId + "/documents/" + documentId;

        HttpEntity<String> entity = new HttpEntity<>(generateAuthHeader());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        return response.getBody();
    }
}
