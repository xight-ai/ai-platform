package com.xight.ai.platform.schedule;

import com.xight.ai.platform.config.FeishuConfig;
import com.xight.ai.platform.model.dify.CreateKnowledgeResult;
import com.xight.ai.platform.model.dify.Document;
import com.xight.ai.platform.model.dify.KnowLedgeInfo;
import com.xight.ai.platform.model.dify.Knowledge;
import com.xight.ai.platform.model.feishu.*;
import com.xight.ai.platform.model.ragflow.CreateDatasetResult;
import com.xight.ai.platform.model.ragflow.Dataset;
import com.xight.ai.platform.model.ragflow.RagflowDocument;
import com.xight.ai.platform.model.ragflow.UploadDocResult;
import com.xight.ai.platform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DocExportSchedule {

    @Autowired
    private FeishuService feishuService;

    @Autowired
    private DifyHttpApi difyHttpApi;

    @Autowired
    private DifyService difyService;

    @Autowired
    private RagFlowHttpApi ragFlowHttpApi;

    @Autowired
    private RagFlowService ragFlowService;

    @Autowired
    private RedisService redisService;

    @Value("${dify.supportFiletype}")
    private String supportFiletypes;

    @Value("${feishu.exportPath}")
    private String exportPath;

    @Autowired
    private FeishuConfig feishuConfig;

    @Autowired
    private FeishuHttpApi feishuHttpApi;

    // 将知识库同步至dify
    //    @Scheduled(cron = "0 10 0 * * *")
    public void uploadToDify() {
        System.out.println("开始导出！");
        List<WikiSpace> wikiSpaceList = feishuService.getAllWikiSpaces();
        for (WikiSpace wikiSpace : wikiSpaceList) {
            feishuService.exportAllNode(wikiSpace.getSpaceId());
        }
        System.out.println("导出结束！");

        System.out.println("开始上传");
        // 本地知识库路径
        Path path = Paths.get(exportPath);
        // 遍历文件夹中的文件
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            // dify全部知识库
            List<KnowLedgeInfo> knowLedgeInfos = difyService.getKnowledgeList();
            // 存成map，id和标识位
//            Map<String, Boolean> knowLedgeMap = knowLedgeInfos.stream().collect(Collectors.toMap(KnowLedgeInfo::getId, item->false));
            for (Path wikiPath : stream) {
                System.out.println("开始处理知识库: " + wikiPath.getFileName());

                String wikiId = wikiPath.getFileName().toString();
                // 获取对应的知识库
                List<KnowLedgeInfo> infos = knowLedgeInfos.stream().filter(info -> info.getName().equals(wikiId)).collect(Collectors.toList());
                if (!infos.isEmpty()) {
                    // 有就更新
                    KnowLedgeInfo knowLedgeInfo = infos.get(0);
                    String knowLedgeId = knowLedgeInfo.getId();
                    // 标记为处理过
//                    knowLedgeMap.put(knowLedgeId,true);
                    List<Document> documentList = difyService.getDocumentList(knowLedgeId);

                    Stream<Path> wikiStream = Files.walk(wikiPath);
                    wikiStream.forEach(file -> {
                        System.out.println("遍历文件：" + file.toAbsolutePath());
                        if (Files.isRegularFile(file) && !Files.isDirectory(file)) {
                            String filePath = file.toAbsolutePath().toString();
                            System.out.println("处理文件：" + filePath);

                            int dotIndex = filePath.lastIndexOf(".");
                            if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
                                String ext = filePath.substring(dotIndex + 1);
                                // 判断文件格式是否支持
                                if (Arrays.stream(supportFiletypes.toLowerCase().split(",")).collect(Collectors.toList()).contains(ext)) {
                                    List<Document> docs = documentList.stream().filter(document -> document.getName().equals(file.getFileName().toString())).collect(Collectors.toList());
                                    if (!docs.isEmpty()) {
                                        // 有就更新
                                        difyHttpApi.updateDocByFile(knowLedgeId, docs.get(0).getId(), null, file.toAbsolutePath().toString());
                                    } else {
                                        //没有就创建
                                        difyService.createDocByFile(knowLedgeId, file.toAbsolutePath().toString());
                                    }
                                }
                            }
                        }
                    });
                } else {
                    // 没有就创建
                    Knowledge knowledge = new Knowledge();
                    knowledge.setName(wikiId);
                    knowledge.setIndexingTechnique("high_quality");
                    knowledge.setPermission("only_me");
                    CreateKnowledgeResult createKnowledgeResult = difyHttpApi.createEmptyKnowledge(knowledge);

                    Stream<Path> wikiStream = Files.walk(wikiPath);
                    wikiStream.forEach(file -> {
                        if (Files.isRegularFile(file)) {
                            // 创建文档
                            String filePath = file.toAbsolutePath().toString();
                            System.out.println("处理文件：" + filePath);

                            int dotIndex = filePath.lastIndexOf(".");
                            if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
                                String ext = filePath.substring(dotIndex + 1);
                                if (Arrays.stream(supportFiletypes.toLowerCase().split(",")).collect(Collectors.toList()).contains(ext)) {
                                    difyService.createDocByFile(createKnowledgeResult.getId(), file.toAbsolutePath().toString());
                                }
                            }
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("结束上传");
    }

    //  将知识库同步至ragflow
    @Scheduled(cron = "0 10 0 * * *")
    public void uploadToRagFlow() throws IOException {

        System.out.println("开始测试导出！");

        List<WikiSpace> wikiSpaceList = feishuService.getAllWikiSpaces();

        for (WikiSpace wikiSpace : wikiSpaceList) {
            String rootPath = feishuConfig.getExportPath() + "/" + wikiSpace.getSpaceId() + "/";
            List<Dataset> infos = ragFlowHttpApi.listDatasets(null, wikiSpace.getSpaceId());
            Dataset datasetInfo;
            if (infos == null || infos.isEmpty()) {
                // 没有就创建
                CreateDatasetResult createDatasetResult = ragFlowHttpApi.createDataSet(wikiSpace.getSpaceId());
                datasetInfo = createDatasetResult.getData();
            } else {
                datasetInfo = infos.get(0);
            }

            String datasetId = datasetInfo.getId();

            // 获取知识库下全部节点
            List<WikiNodeItem> nodes = feishuService.getAllWikiNode(wikiSpace.getSpaceId());
            for (WikiNodeItem item : nodes) {
                File file = null;
                byte[] bytes;
                // 只导出了 docx 类型的文档
                if (item.getObjType().equals("docx") || item.getObjType().equals("doc")) {
                    System.out.println("导出文件：" + item.getTitle());
                    bytes = feishuService.downloadDocument(feishuConfig.getDocSaveType(), item.getObjToken(), item.getObjType());
                    if (bytes != null) {
                        file = new File(rootPath + item.getTitle().replaceAll("[:”，\"]", "") + "." + feishuConfig.getDocSaveType());
                    }
                } else if (item.getObjType().equals("file")) {
                    // 文件类型直接下载
                    System.out.println("下载文件：" + item.getTitle());
                    bytes = feishuHttpApi.downLoadFile(item.getObjToken());
                    if (bytes != null) {
                        file = new File(rootPath + item.getTitle().replaceAll("[:”，\"]", ""));
                    }
                } else {
                    continue;
                }
                String fileName = file.getName();
                System.out.println("文件名：" + fileName);
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    if (parentDir.mkdirs()) {
                        System.out.println("目录创建成功");
                    } else {
                        System.err.println("目录创建失败");
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                    System.out.println("文件: " + fileName + "保存成功！");

                    List<RagflowDocument> allDocs = ragFlowService.searchDocuments(datasetId, null, null);

                    List<RagflowDocument> docs = allDocs.stream().filter(tempDoc -> tempDoc.getName().equals(fileName)).collect(Collectors.toList());

                    if (!docs.isEmpty()) {
                        // 单位秒
                        long updateTime = Long.parseLong(item.getObjEditTime());
                        System.out.println("updateTime:" + updateTime);

                        RagflowDocument thisDoc = docs.get(0);
                        System.out.println("createTime:" + thisDoc.getCreateTime());
                        if (thisDoc.getCreateTime() < updateTime * 1000) {
                            // 在ragflow文档上传后有更新过
                            // 有就删除再上传
                            ragFlowHttpApi.deleteDocument(datasetId, docs.stream().map(RagflowDocument::getId).collect(Collectors.toList()));
                            Thread.sleep(1000);
                            UploadDocResult uploadDocResult = ragFlowHttpApi.uploadDocuments(datasetId, file.getAbsolutePath());
                            if (uploadDocResult != null && uploadDocResult.getData() != null) {
                                ragFlowHttpApi.parseDocument(datasetId, uploadDocResult.getData().stream().map(RagflowDocument::getId).collect(Collectors.toList()));
                            }
                        }
                    } else {
                        //没有就直接创建
                        UploadDocResult uploadDocResult = ragFlowHttpApi.uploadDocuments(datasetId, file.getAbsolutePath());
                        if (uploadDocResult != null && uploadDocResult.getData() != null) {
                            ragFlowHttpApi.parseDocument(datasetId, uploadDocResult.getData().stream().map(RagflowDocument::getId).collect(Collectors.toList()));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("文件保存失败了！");
                }
            }
        }
        System.out.println("测试导出结束！");
    }

    /**
     * 启动同步知识库缓存
     */
//    @PostConstruct
    public void initWikiCache() {
        syncWikiCache();
    }

    /**
     * 每晚0点同步知识库缓存
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledSyncWikiCache() {
        syncWikiCache();
    }

    /**
     * 为了提升接口访问速率，使用redis缓存知识库信息
     */
    public void syncWikiCache() {
        List<WikiCache> wikiCacheList = new ArrayList<>();
        // 获取全部飞书知识库
        List<WikiSpace> allSpaceList = feishuService.getAllWikiSpaces();
        for (WikiSpace space : allSpaceList) {
            // WikiCache是用于缓存的对象，里面包含了知识库成员
            WikiCache wikiCache = new WikiCache();
            List<WikiSpaceMember> wikiSpaceMembers = new ArrayList<>();
            // 获取知识库的成员
            List<WikiSpaceMember> members = feishuService.getWikiSpaceMembersBySpaceId(space.getSpaceId());
            for (WikiSpaceMember member : members) {
                // 如果是oc开始的群，添加群成员
                if (member.getMemberId().startsWith("oc")) {
                    List<ChatMember> chatMembers = feishuService.getChatMembersByChatId(member.getMemberId());
                    for (ChatMember chatMember : chatMembers) {
                        WikiSpaceMember wikiSpaceMember = new WikiSpaceMember();
                        wikiSpaceMember.setMemberId(chatMember.getMemberId());
                        wikiSpaceMember.setMemberType(chatMember.getMemberIdType());
                        wikiSpaceMember.setMemberRole(member.getMemberRole());
                        wikiSpaceMembers.add(wikiSpaceMember);
                    }
                } else if (member.getMemberId().startsWith("od")) {
                    // 若是od开始的部门，添加部门成员
                    List<DepartmentUser> departmentUsers = feishuService.getDepartmentUsersByDepartmentId(member.getMemberId());
                    for (DepartmentUser departmentUser : departmentUsers) {
                        WikiSpaceMember wikiSpaceMember = new WikiSpaceMember();
                        wikiSpaceMember.setMemberId(departmentUser.getOpenId());
                        wikiSpaceMember.setMemberType(member.getMemberType());
                        wikiSpaceMember.setMemberRole(member.getMemberRole());
                        wikiSpaceMembers.add(wikiSpaceMember);
                    }
                } else {
                    // 普通成员就直接添加
                    wikiSpaceMembers.add(member);
                }
            }
            wikiCache.setMembers(wikiSpaceMembers);
            wikiCache.setWikiSpace(space);
            wikiCacheList.add(wikiCache);
            String key = "wikiCache:space:" + space.getSpaceId();
            // 单个知识库的缓存
            redisService.set(key, wikiCache);
        }
        // 全知识库的缓存
        redisService.set("wikiCache:allSpace", wikiCacheList);
    }
}
