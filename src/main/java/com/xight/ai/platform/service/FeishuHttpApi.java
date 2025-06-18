package com.xight.ai.platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xight.ai.platform.config.FeishuConfig;
import com.xight.ai.platform.model.feishu.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FeishuHttpApi {

    @Autowired
    private FeishuConfig feishuConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取自建应用的AccessToken的请求
     *
     * @return token
     */
    private AccessToken getTenantAccessToken() {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppId(feishuConfig.getAppId());
        appInfo.setAppSecret(feishuConfig.getAppSecret());
        String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/auth/v3/tenant_access_token/internal";
        ResponseEntity<AccessToken> response = restTemplate.postForEntity(url, appInfo, AccessToken.class);
        return response.getBody();
    }

    /**
     * 获取应用token字符串
     *
     * @return 应用token字符串
     */
    private String generateAccessToken() {
        // todo 修改成不过期就用原token
        return getTenantAccessToken().getTenantAccessToken();
    }

    /**
     * 获取有应用token的header
     *
     * @return header
     */
    private HttpHeaders generateAuthHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(generateAccessToken());
        return httpHeaders;
    }

    /**
     * 使用应用token获取当前权限下的所有的知识库
     *
     * @return 所有的知识库列表
     */
    public ResponseData<PageResult<WikiSpace>> getWikiSpaces(String pageToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/wiki/v2/spaces?page_size=50";
            if (pageToken != null && !pageToken.isEmpty()) {
                url = url + "&page_token=" + pageToken;
            }

            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<PageResult<WikiSpace>>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch WikiSpaces", e);
        }
    }

    /**
     * 获取某个知识库详细信息
     *
     * @param spaceId 知识库id
     * @return
     */
    public ResponseData<WikiSpaceInfo> getWikiSpaceInfo(String spaceId) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/wiki/v2/spaces/{spaceId}";

            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class, spaceId);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<WikiSpaceInfo>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch WikiSpaceInfo", e);
        }
    }

    /**
     * 获取知识空间子节点列表
     *
     * @param spaceId         知识库id
     * @param pageToken       分页标记，第一次请求不填，表示从头开始遍历；
     * @param parentNodeToken 父节点token
     * @return 知识空间子节点列表
     */
    public ResponseData<PageResult<WikiNodeItem>> getWikiNodeList(String spaceId, String pageToken, String parentNodeToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/wiki/v2/spaces/" + spaceId + "/nodes?page_size=50";
            if (pageToken != null && !pageToken.isEmpty()) {
                url += "&page_token=" + pageToken;
            }
            if (parentNodeToken != null && !parentNodeToken.isEmpty()) {
                url += "&parent_node_token=" + parentNodeToken;
            }

            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<PageResult<WikiNodeItem>>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch WikiNodeList", e);
        }
    }

    /**
     * 创建文档导出任务结果
     *
     * @param taskParam 任务参数
     * @return 导出结果
     */
    public ResponseData<ExportOutput> createExportTask(ExportTaskParam taskParam) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/drive/v1/export_tasks";

            HttpEntity<ExportTaskParam> httpEntity = new HttpEntity<>(taskParam, generateAuthHeader());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<ExportOutput>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create ExportTask", e);
        }
    }

    /**
     * 查询导出任务
     *
     * @param ticket
     * @param token
     * @return
     */
    public ResponseData<ExportTaskResult> queryExportTask(String ticket, String token) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/drive/v1/export_tasks/{ticket}?token={token}";

            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class, ticket, token);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<ExportTaskResult>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to query ExportTask", e);
        }
    }

    /**
     * 下载导出文件
     *
     * @param fileToken 文件token
     * @return 文件数据
     */
    public byte[] downLoad(String fileToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/drive/v1/export_tasks/file/{fileToken}/download";

            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class, fileToken);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载文件
     *
     * @param fileToken 文件token
     * @return 文件数据
     */
    public byte[] downLoadFile(String fileToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/drive/v1/files/{fileToken}/download";

            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class, fileToken);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据授权码获取用户的访问凭证
     *
     * @param code 授权码
     * @return 用户的访问凭证
     */
    public UserAccessTokenResponse getUserAccessToken(String code) {
        String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/authen/v2/oauth/token";

        UserAccessTokenRequest userAccessTokenRequest = new UserAccessTokenRequest();
        userAccessTokenRequest.setCode(code);
        userAccessTokenRequest.setClientId(feishuConfig.getAppId());
        userAccessTokenRequest.setClientSecret(feishuConfig.getAppSecret());
        userAccessTokenRequest.setRedirectUri(feishuConfig.getRedirectUri());

        HttpEntity<UserAccessTokenRequest> httpEntity = new HttpEntity<>(userAccessTokenRequest);
        ResponseEntity<UserAccessTokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, UserAccessTokenResponse.class);
        return response.getBody();
    }

    /**
     * 根据user_access_token获取用户信息
     *
     * @param userAccessToken user_access_token
     * @return 用户信息
     */
    public ResponseData<FeishuUserInfo> getUserInfo(String userAccessToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/authen/v1/user_info";

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setBearerAuth(userAccessToken);
            HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<FeishuUserInfo>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch WikiSpaces", e);
        }
    }

    /**
     * 根据user_access_token获取可用的知识库
     *
     * @return 所有的知识库列表
     */
    public ResponseData<PageResult<WikiSpace>> getWikiSpacesByUserToken(String userAccessToken, String pageToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/wiki/v2/spaces?page_size=50";
            if (pageToken != null && !pageToken.isEmpty()) {
                url = url + "&page_token=" + pageToken;
            }

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setBearerAuth(userAccessToken);

            HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<PageResult<WikiSpace>>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch WikiSpaces", e);
        }
    }

    /**
     * 获取单个审批实例详情
     *
     * @param instanceId 审批实例id
     * @return 审批实例的详情
     */
    public ResponseData<ApprovalInstanceInfo> getApprovalInstanceInfo(String instanceId) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/approval/v4/instances/" + instanceId;

            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<ApprovalInstanceInfo>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取知识库成员列表
     *
     * @param spaceId   知识库id
     * @param pageToken 分页标记
     * @return 知识库成员列表
     */
    public ResponseData<PageMembersResult> getWikiSpacesMembers(String spaceId, String pageToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/wiki/v2/spaces/" + spaceId + "/members?page_size=50";
            if (pageToken != null && !pageToken.isEmpty()) {
                url = url + "&page_token=" + pageToken;
            }
            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<PageMembersResult>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取群组成员
     *
     * @param chatId    群组id
     * @param pageToken 页面token
     * @return 群组成员
     */
    public ResponseData<PageResult<ChatMember>> getChatMembers(String chatId, String pageToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/im/v1/chats/" + chatId + "/members?page_size=50&member_id_type=open_id";
            if (pageToken != null && !pageToken.isEmpty()) {
                url = url + "&page_token=" + pageToken;
            }
            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<PageResult<ChatMember>>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取子部门
     *
     * @param departmentId 部门id
     * @param pageToken    页面token
     * @return 所有子部门
     */
    public ResponseData<PageResult<Department>> getChildDepartments(String departmentId, String pageToken) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/contact/v3/departments/" + departmentId + "/children?page_size=50&fetch_child=true";
            if (pageToken != null && !pageToken.isEmpty()) {
                url = url + "&page_token=" + pageToken;
            }
            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<PageResult<Department>>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取单个部门信息
     *
     * @param departmentId 部门id
     * @return 该部门信息
     */
    public ResponseData<SingleDepartment> getSingleDepartment(String departmentId) {
        try {
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/contact/v3/departments/" + departmentId;
            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<SingleDepartment>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取部门直属用户列表
     *
     * @param departmentId 部门id
     * @param pageToken    页面token
     * @return 部门直属用户
     */
    public ResponseData<PageResult<DepartmentUser>> getDepartmentUsers(String departmentId, String pageToken) {
        try {
            System.out.println("****************" + departmentId);
            String url = feishuConfig.getOpenApiEndPoint() + "/open-apis/contact/v3/users/find_by_department?department_id=" + departmentId + "&page_size=50";
            if (pageToken != null && !pageToken.isEmpty()) {
                url = url + "&page_token=" + pageToken;
            }
            HttpEntity<String> httpEntity = new HttpEntity<>(generateAuthHeader());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String jsonResult = response.getBody();
            return objectMapper.readValue(jsonResult, new TypeReference<ResponseData<PageResult<DepartmentUser>>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
