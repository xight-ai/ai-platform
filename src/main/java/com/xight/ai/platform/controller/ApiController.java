package com.xight.ai.platform.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xight.ai.platform.model.common.Result;
import com.xight.ai.platform.model.dify.KnowLedgeInfo;
import com.xight.ai.platform.model.dify.QueryRecord;
import com.xight.ai.platform.model.feishu.ApprovalSaved;
import com.xight.ai.platform.model.feishu.FeishuPrincipal;
import com.xight.ai.platform.model.feishu.WikiOption;
import com.xight.ai.platform.model.feishu.WikiSpace;
import com.xight.ai.platform.model.ragflow.Chunk;
import com.xight.ai.platform.model.ragflow.Dataset;
import com.xight.ai.platform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/api")
public class ApiController {

    @Autowired
    private DifyService difyService;

    @Autowired
    private DifyHttpApi difyHttpApi;

    @Autowired
    private FeishuService feishuService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RagFlowHttpApi ragFlowHttpApi;

    /**
     * 获取当前登录用户所能看到的飞书知识库
     *
     * @return 返回下拉框列表选项
     */
    @GetMapping(value = "getMyWikis")
    public List<WikiOption> getMyWikis() throws JsonProcessingException {
        List<WikiOption> options = new ArrayList<>();

        //当前登录用户自己的权限下能看到的知识库
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        FeishuPrincipal feishuPrincipal = ((FeishuPrincipal) authentication.getPrincipal());
        // redis
        List<WikiSpace> wikiSpaces = feishuService.getWikiSpacesByUserId(feishuPrincipal.getUserId());

        // 被审批允许查看的知识库
        List<WikiSpace> approvalWikiSpaces = new ArrayList<>();
        String redisKey = "approval:" + feishuPrincipal.getUserId();
        String redisValue = (String) redisService.get(redisKey);
        if (redisValue != null && !redisValue.isEmpty()) {
            List<ApprovalSaved> approvalSavedList = objectMapper.readValue(redisValue, new TypeReference<List<ApprovalSaved>>() {
            });
            // 从redis获取已审批通过的对象的id
            HashSet<String> openIds = new HashSet<>();
            if (approvalSavedList != null && !approvalSavedList.isEmpty()) {
                for (ApprovalSaved approvalSaved : approvalSavedList) {
                    String expireStr = approvalSaved.getExpireTime();
                    ZonedDateTime expireDateTime = ZonedDateTime.parse(expireStr, DateTimeFormatter.ISO_ZONED_DATE_TIME).plusDays(1);
                    ZonedDateTime now = ZonedDateTime.now();
                    // 确认时间没有过期
                    if (now.isBefore(expireDateTime)) {
                        openIds.addAll(approvalSaved.getOpenIds());
                    }
                }
            }
            // 从redis获取
            approvalWikiSpaces = feishuService.getRedisWikiSpacesByAdminIds(openIds);
        }

        // 混合两种知识库
        List<WikiSpace> mergedWikiSpaces = Stream.concat(wikiSpaces.stream(), approvalWikiSpaces.stream()).collect(Collectors.toList());

        // 获取自己记忆的数据库选择
        String savedOptions = (String) redisService.get(feishuPrincipal.getUserId());
        List<String> selectedOptions = new ArrayList<>();
        if (StringUtils.hasLength(savedOptions)) {
            selectedOptions = Arrays.stream(savedOptions.split(",")).map(String::trim).collect(Collectors.toList());
        }
        for (WikiSpace wikiSpace : mergedWikiSpaces) {
            WikiOption option = new WikiOption();
            String wikiId = wikiSpace.getSpaceId();
            option.setWikiId(wikiId);
            option.setWikiName(wikiSpace.getName());
            if (selectedOptions.contains(wikiId)) {
                option.setSelected(true);
            }
            options.add(option);
        }

        // 返回下拉框列表选项
        return options;
    }

    /**
     * 使用飞书知识库wikiId来检索对应的dify知识库
     *
     * @param wikiIds 用户id+飞书知识库id，id之间用逗号隔开
     * @param query   检索的问题
     * @return 检索到的结果列表
     */
    @GetMapping(value = "/queryByWikiIds")
    public List<QueryRecord> queryByWiki(String wikiIds, String query) {
        if (!StringUtils.hasLength(wikiIds)) {
            return null;
        }
        List<QueryRecord> results = new ArrayList<>();
        String[] totalList = wikiIds.split(",");
        // 获取飞书知识库id数组
        String[] wikiIdList = Arrays.copyOfRange(totalList, 1, totalList.length);
        // 获取dify全部的知识库
        List<KnowLedgeInfo> knowLedgeInfos = difyService.getKnowledgeList();
        for (String wikiId : wikiIdList) {
            // 根据飞书知识库id滤出dify知识库
            List<KnowLedgeInfo> infos = knowLedgeInfos.stream().filter(info -> info.getName().equals(wikiId)).collect(Collectors.toList());
            if (!infos.isEmpty()) {
                String knowledgeId = infos.get(0).getId();
                results.addAll(difyHttpApi.queryKnowledge(knowledgeId, query).getRecords());
            }
        }
        return results;
    }

    /**
     * 使用飞书知识库wikiId来检索对应的ragflow知识库
     *
     * @param wikiIds 用户id+飞书知识库id，id之间用逗号隔开
     * @param query   检索的问题
     * @return 检索到的结果列表
     */
    @GetMapping(value = "/queryFromRagFlow")
    public List<Chunk> queryFromRagFlow(String wikiIds, String query) {
        if (!StringUtils.hasLength(wikiIds)) {
            return null;
        }
        String[] totalList = wikiIds.split(",");
        // 获取飞书知识库id数组
        String[] wikiIdList = Arrays.copyOfRange(totalList, 1, totalList.length);
        List<String> datasetIds = new ArrayList<>();
        // 获取dify全部的知识库
        List<Dataset> datasets = ragFlowHttpApi.listDatasets(null, null);
        for (String wikiId : wikiIdList) {
            List<Dataset> infos = datasets.stream().filter(info -> info.getName().equals(wikiId)).collect(Collectors.toList());
            if (!infos.isEmpty()) {
                String knowledgeId = infos.get(0).getId();
                datasetIds.add(knowledgeId);
            }
        }
        return ragFlowHttpApi.retrieveChunks(query, datasetIds);
    }

    /**
     * 保存知识库选项
     *
     * @param wikiIds 要保存的知识库id
     * @return 保存结果
     */
    @PostMapping("/saveMyWikiOptions")
    public Result saveMyWikiOptions(String wikiIds) {
        Result result = new Result();
        result.setMessage("success");
        if (!StringUtils.hasLength(wikiIds)) {
            return result;
        }
        // 获取当前登录用户来设置redis保存知识库id
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        FeishuPrincipal feishuPrincipal = ((FeishuPrincipal) authentication.getPrincipal());
        redisService.set(feishuPrincipal.getUserId(), wikiIds);
        return result;
    }
}
