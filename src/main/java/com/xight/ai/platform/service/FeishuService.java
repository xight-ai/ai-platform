package com.xight.ai.platform.service;


import com.xight.ai.platform.config.FeishuConfig;
import com.xight.ai.platform.model.feishu.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeishuService {

    @Autowired
    private FeishuConfig feishuConfig;

    @Autowired
    private FeishuHttpApi feishuHttpApi;

    @Autowired
    private RedisService redisService;

    /**
     * 获取当前应用权限下的全部知识库
     *
     * @return 全部的知识库列表
     */
    public List<WikiSpace> getAllWikiSpaces() {
        List<WikiSpace> wikiSpaces = new ArrayList<>();
        String pageToken = null;
        boolean hasMore;
        do {
            PageResult<WikiSpace> pageResult = feishuHttpApi.getWikiSpaces(pageToken).getData();
            wikiSpaces.addAll(pageResult.getItems());
            pageToken = pageResult.getPageToken();
            hasMore = pageResult.isHasMore();
        } while (hasMore && StringUtils.hasLength(pageToken));
        return wikiSpaces;
    }

    /**
     * 根据知识库id获取全部的节点
     *
     * @param spaceId 知识库id
     * @return
     */
    public List<WikiNodeItem> getAllWikiNode(String spaceId) {
        List<WikiNodeItem> nodes = new ArrayList<>();
        String pageToken = null;
        boolean hasMore;

        do {
            PageResult<WikiNodeItem> pageResult = feishuHttpApi.getWikiNodeList(spaceId, pageToken, null).getData();
            nodes.addAll(pageResult.getItems());
            for (WikiNodeItem item : pageResult.getItems()) {
                if (item.isHasChild()) {
                    List<WikiNodeItem> childNodes = getWikiChildNode(spaceId, item.getNodeToken());
                    nodes.addAll(childNodes);
                }
            }
            pageToken = pageResult.getPageToken();
            hasMore = pageResult.isHasMore();
        } while (hasMore && StringUtils.hasLength(pageToken));

        // 清除了标题为空的未命名文档
        nodes.removeIf(item -> !StringUtils.hasLength(item.getTitle()));

        return nodes;
    }

    /**
     * 获取子节点
     *
     * @param spaceId         知识库id
     * @param parentNodeToken 父节点
     * @return
     */
    public List<WikiNodeItem> getWikiChildNode(String spaceId, String parentNodeToken) {
        List<WikiNodeItem> childNodes = new ArrayList<>();
        String pageToken = null;
        boolean hasMore;
        do {
            PageResult<WikiNodeItem> pageResult = feishuHttpApi.getWikiNodeList(spaceId, pageToken, parentNodeToken).getData();
            childNodes.addAll(pageResult.getItems());

            for (WikiNodeItem item : pageResult.getItems()) {
                if (item.isHasChild()) {
                    List<WikiNodeItem> grandChildNodes = getWikiChildNode(spaceId, item.getNodeToken());
                    childNodes.addAll(grandChildNodes);
                }
            }

            pageToken = pageResult.getPageToken();
            hasMore = pageResult.isHasMore();
        } while (hasMore && StringUtils.hasLength(pageToken));
        return childNodes;
    }

    /**
     * 导出某一个知识库的全部文件
     *
     * @param spaceId 知识库的id
     */
    public void exportAllNode(String spaceId) {
        // 获取知识库下全部节点
        List<WikiNodeItem> nodes = getAllWikiNode(spaceId);

        // 生成导出路径,这里注释掉了
//        Map<String, String> pathMap = generateExportPathMap(globalConfig.getExportPath()+"/" + spaceId, nodes);
        String rootPath = feishuConfig.getExportPath() + "/" + spaceId + "/";

        for (WikiNodeItem item : nodes) {
            // 只导出了 docx 类型的文档
            if (item.getObjType().equals("docx") || item.getObjType().equals("doc")) {
                System.out.println("导出文件：" + item.getTitle());
                byte[] bytes = downloadDocument(feishuConfig.getDocSaveType(), item.getObjToken(), item.getObjType());
                if (bytes != null) {
                    try {
//                        File file = new File(pathMap.get(item.getObjToken()) + "." + globalConfig.getDocSaveType());
                        File file = new File(rootPath + item.getTitle().replaceAll("[:”，\"]", "") + "." + feishuConfig.getDocSaveType());
                        System.out.println("文件名：" + file.getName());
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
                            System.out.println("文件保存成功！");
                        }
                    } catch (Exception e) {
                        System.err.println("文件保存失败：" + e.getMessage());
                    }
                }
            } else if (item.getObjType().equals("file")) {
                // 文件类型直接下载
                System.out.println("下载文件：" + item.getTitle());
                byte[] bytes = feishuHttpApi.downLoadFile(item.getObjToken());
                if (bytes != null) {
                    try {
//                        File file = new File(pathMap.get(item.getObjToken()));
                        File file = new File(rootPath + item.getTitle().replaceAll("[:”，\"]", ""));
                        System.out.println("文件名：" + file.getName());
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
                            System.out.println("文件保存成功！");
                        }
                    } catch (Exception e) {
                        System.err.println("文件保存失败：" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 下载具体一个文件
     *
     * @param fileExtension 保存文件的后缀
     * @param objToken      导出文件的objToken
     * @param type          导出文件的类型
     * @return
     */
    public byte[] downloadDocument(String fileExtension, String objToken, String type) {
        ExportTaskParam taskParam = new ExportTaskParam();
        taskParam.setFileExtension(fileExtension);
        taskParam.setType(type);
        taskParam.setToken(objToken);
        ExportOutput exportOutput = feishuHttpApi.createExportTask(taskParam).getData();

        int maxRetryCount = 10;
        ExportTaskResult taskResult = new ExportTaskResult();
        for (int i = 0; i < maxRetryCount; i++) {
            try {
                taskResult = feishuHttpApi.queryExportTask(exportOutput.getTicket(), objToken).getData();

                if (taskResult.getResult().getJobStatus() > 0) {
                    Thread.sleep(1000);
                } else {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ExportTaskInfo taskInfo = taskResult.getResult();

        if (taskInfo.getJobErrorMsg().equals("success")) {
            byte[] bytes = feishuHttpApi.downLoad(taskInfo.getFileToken());
            return bytes;
        }
        return null;
    }

    /**
     * 生成导出路径的完整map映射，文档objToken和导出路径的映射
     *
     * @param rootFolderPath 根路径，即最外层的路径
     * @param nodes          全部的文档节点
     * @return
     */
    private Map<String, String> generateExportPathMap(String rootFolderPath, List<WikiNodeItem> nodes) {
        Map<String, String> exportPathMap = new HashMap<>();

        List<WikiNodeItem> topNodes = nodes.stream().filter(node -> !StringUtils.hasLength(node.getParentNodeToken())).collect(Collectors.toList());

        for (WikiNodeItem item : topNodes) {
            exportPathMap.putAll(generateExportChildPathMap(item, rootFolderPath, nodes, exportPathMap));
        }
        return exportPathMap;
    }

    /**
     * 生成导出路径的map子映射，文档objToken和导出路径的映射
     *
     * @param node             父node
     * @param parentFolderPath 父node的导出路径
     * @param nodes            全部的文档节点
     * @param exportPathMap    映射map
     * @return
     */
    private Map<String, String> generateExportChildPathMap(WikiNodeItem node, String parentFolderPath, List<WikiNodeItem> nodes, Map<String, String> exportPathMap) {
        String nodePath = parentFolderPath + "/" + node.getTitle();

        exportPathMap.put(node.getObjToken(), nodePath);

        List<WikiNodeItem> childNodes = nodes.stream().filter(childNode -> childNode.getParentNodeToken().equals(node.getNodeToken())).collect(Collectors.toList());

        for (WikiNodeItem item : childNodes) {
            exportPathMap.putAll(generateExportChildPathMap(item, nodePath, nodes, exportPathMap));
        }
        return exportPathMap;
    }

    /**
     * 根据用户获取他能看的知识库
     *
     * @param userAccessToken 飞书认证
     * @return
     */
    public List<WikiSpace> getWikiSpacesByUserToken(String userAccessToken) {
        List<WikiSpace> spaceList = new ArrayList<>();

        String pageToken = null;
        boolean hasMore;

        do {
            PageResult<WikiSpace> pageResult = feishuHttpApi.getWikiSpacesByUserToken(userAccessToken, pageToken).getData();
            spaceList.addAll(pageResult.getItems());
            hasMore = pageResult.isHasMore();
            pageToken = pageResult.getPageToken();
        } while (hasMore);

        return spaceList;
    }

    /**
     * 在本应用能看到的知识库下找出某些用户拥有的知识库
     * 这个接口太慢了
     *
     * @param adminIds 用户openid列表
     * @return 该用户拥有的知识库
     */
    public List<WikiSpace> getWikiSpacesByAdminIds(HashSet<String> adminIds) {
        List<WikiSpace> spaceList = new ArrayList<>();
        List<WikiSpace> allSpaceList = getAllWikiSpaces();
        for (WikiSpace space : allSpaceList) {
            List<WikiSpaceMember> members = getWikiSpaceMembersBySpaceId(space.getSpaceId());
            List<String> memberIds = members.stream().filter(member -> member.getMemberRole().equals("admin")).map(WikiSpaceMember::getMemberId).collect(Collectors.toList());
            if (!Collections.disjoint(memberIds, adminIds)) {
                spaceList.add(space);
            }
        }
        return spaceList;
    }

    /**
     * 获取知识库的成员列表
     *
     * @param spaceId 知识库id
     * @return 知识库成员列表
     */
    public List<WikiSpaceMember> getWikiSpaceMembersBySpaceId(String spaceId) {
        List<WikiSpaceMember> memberList = new ArrayList<>();

        String pageToken = null;
        boolean hasMore;

        do {
            PageMembersResult pageMembersResult = feishuHttpApi.getWikiSpacesMembers(spaceId, pageToken).getData();
            memberList.addAll(pageMembersResult.getMembers());
            hasMore = pageMembersResult.isHasMore();
            pageToken = pageMembersResult.getPageToken();
        } while (hasMore);

        return memberList;
    }

    /**
     * 获取群组的成员
     *
     * @param chatId 群组id
     * @return 群组成员列表
     */
    public List<ChatMember> getChatMembersByChatId(String chatId) {
        List<ChatMember> chatMemberList = new ArrayList<>();

        String pageToken = null;
        boolean hasMore;

        do {
            PageResult<ChatMember> pageResult = feishuHttpApi.getChatMembers(chatId, pageToken).getData();
            chatMemberList.addAll(pageResult.getItems());
            hasMore = pageResult.isHasMore();
            pageToken = pageResult.getPageToken();
        } while (hasMore);
        return chatMemberList;
    }

    /**
     * 根据部门id获取其下所有成员
     *
     * @param departmentId 部门id
     * @return 所有成员
     */
    public List<DepartmentUser> getDepartmentUsersByDepartmentId(String departmentId) {
        Department department = feishuHttpApi.getSingleDepartment(departmentId).getData().getDepartment();

        List<Department> departmentList = new ArrayList<>();
        String pageToken = null;
        boolean hasMore;
        do {
            PageResult<Department> pageResult = feishuHttpApi.getChildDepartments(departmentId, pageToken).getData();
            if (pageResult.getItems() != null) {
                departmentList.addAll(pageResult.getItems());
                pageToken = pageResult.getPageToken();
            }
            hasMore = pageResult.isHasMore();
        } while (hasMore);
        departmentList.add(department);

        List<DepartmentUser> departmentUsers = new ArrayList<>();

        for (Department d : departmentList) {
            String pageToken2 = null;
            boolean hasMore2;
            do {
                PageResult<DepartmentUser> pageResult = feishuHttpApi.getDepartmentUsers(d.getOpenDepartmentId(), pageToken2).getData();
                if (pageResult.getItems() != null) {
                    departmentUsers.addAll(pageResult.getItems());
                    pageToken2 = pageResult.getPageToken();
                }
                hasMore2 = pageResult.isHasMore();
            } while (hasMore2);
        }
        return departmentUsers;
    }

    /**
     * 根据用户Id获取他能看到的知识库
     * 从redis获取
     *
     * @param userId 用户id
     * @return 该用户权限下能看到的知识库
     */
    public List<WikiSpace> getWikiSpacesByUserId(String userId) {
        List<WikiSpace> spaceList = new ArrayList<>();
        List<WikiCache> wikiCacheList = (List<WikiCache>) redisService.get("wikiCache:allSpace");
        for (WikiCache wikiCache : wikiCacheList) {
            // 只要member里有就表示可以看
            boolean isMatch = wikiCache.getMembers().stream().anyMatch(wikiSpaceMember -> wikiSpaceMember.getMemberId().equals(userId));
            if (isMatch) {
                spaceList.add(wikiCache.getWikiSpace());
            }
        }
        return spaceList;
    }

    /**
     * 根据用户id找出其为admin的知识库
     * 从redis获取
     *
     * @param adminIds 用户id集合
     * @return 知识库列表
     */
    public List<WikiSpace> getRedisWikiSpacesByAdminIds(HashSet<String> adminIds) {
        List<WikiSpace> spaceList = new ArrayList<>();
        List<WikiCache> wikiCacheList = (List<WikiCache>) redisService.get("wikiCache:allSpace");
        for (WikiCache wikiCache : wikiCacheList) {
            List<String> cachedAdmins = wikiCache.getMembers().stream().filter(wikiSpaceMember -> wikiSpaceMember.getMemberRole().equals("admin")).map(WikiSpaceMember::getMemberId).collect(Collectors.toList());
            if (!Collections.disjoint(cachedAdmins, adminIds)) {
                spaceList.add(wikiCache.getWikiSpace());
            }
        }
        return spaceList;
    }
}
