package com.xight.ai.platform.service;

import com.xight.ai.platform.model.dify.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DifyService {

    @Autowired
    private DifyHttpApi difyHttpApi;

    /**
     * 获取全部的知识库列表
     *
     * @return 全部知识库列表
     */
    public List<KnowLedgeInfo> getKnowledgeList() {
        ArrayList<KnowLedgeInfo> arrayList = new ArrayList<>();
        int page = 1;
        boolean hasMore;
        do {
            DifyPageResult<KnowLedgeInfo> pageResult = difyHttpApi.getKnowledgeList(page);
            arrayList.addAll(pageResult.getData());
            hasMore = pageResult.isHasMore();
            page++;
        } while (hasMore);

        return arrayList;
    }

    /**
     * 获取指定知识库的全部文档
     *
     * @param knowledgeId 知识库id
     * @return 该知识库里全部文档的列表
     */
    public List<Document> getDocumentList(String knowledgeId) {
        ArrayList<Document> arrayList = new ArrayList<>();
        int page = 1;
        boolean hasMore;
        do {
            DifyPageResult<Document> pageResult = difyHttpApi.getDocumentList(knowledgeId, page);
            arrayList.addAll(pageResult.getData());
            hasMore = pageResult.isHasMore();
            page++;
        } while (hasMore);
        return arrayList;
    }

    /**
     * 创建文档
     *
     * @param knowledgeId 知识库id
     * @param filePath    文件路径
     * @return 创建文档成功的文档信息
     */
    public DocumentInfo createDocByFile(String knowledgeId, String filePath) {
        DocumentData documentData = new DocumentData();

        documentData.setIndexingTechnique("high_quality");

        ProcessRuleSegmentation processRuleSegmentation = new ProcessRuleSegmentation();
        processRuleSegmentation.setMaxTokens(1000);
        processRuleSegmentation.setSeparator("\n\n");

        ArrayList<PreProcessingRule> preProcessingRules = new ArrayList<>();
        preProcessingRules.add(new PreProcessingRule("remove_extra_spaces", true));
        preProcessingRules.add(new PreProcessingRule("remove_urls_emails", false));

        ProcessRuleRules processRuleRules = new ProcessRuleRules();
        processRuleRules.setSegmentation(processRuleSegmentation);
        processRuleRules.setPreProcessingRules(preProcessingRules);

        ProcessRule processRule = new ProcessRule();
        processRule.setMode("custom");
        processRule.setRules(processRuleRules);

        documentData.setProcessRule(processRule);

        return difyHttpApi.createDocByFile(knowledgeId, documentData, filePath);
    }
}
