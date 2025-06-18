package com.xight.ai.platform.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lark.oapi.core.request.EventReq;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.CustomEventHandler;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.lark.oapi.ws.Client;
import com.xight.ai.platform.config.FeishuConfig;
import com.xight.ai.platform.model.feishu.ApprovalInstanceInfo;
import com.xight.ai.platform.model.feishu.ApprovalSaved;
import com.xight.ai.platform.model.feishu.EventCallback;
import com.xight.ai.platform.service.FeishuHttpApi;
import com.xight.ai.platform.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ApprovalListener {

    @Autowired
    private FeishuConfig feishuConfig;

    @Autowired
    private FeishuHttpApi feishuHttpApi;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    // onP2MessageReceiveV1 为接收消息 v2.0；onCustomizedEvent 内的 message 为接收消息 v1.0。
    private final EventDispatcher EVENT_HANDLER = EventDispatcher.newBuilder("", "")
            .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                @Override
                public void handle(P2MessageReceiveV1 event) throws Exception {
                    System.out.printf("[ onP2MessageReceiveV1 access ], data: %s\n", Jsons.DEFAULT.toJson(event.getEvent()));
                }
            })
            .onCustomizedEvent("approval_instance", new CustomEventHandler() {
                // 接收审批实例的事件
                @Override
                public void handle(EventReq event) throws Exception {
                    // 获取事件json
                    String eventJson = new String(event.getBody(), StandardCharsets.UTF_8);
                    System.out.printf("[ onCustomizedEvent access ], type: message, data: %s\n", eventJson);
                    EventCallback eventCallback = objectMapper.readValue(eventJson, EventCallback.class);
                    if (eventCallback.getEvent().getStatus().equals("APPROVED")) {
                        // 只处理审批通过的，审批通过就进行授权
                        // 获取审批实例详情
                        ApprovalInstanceInfo approvalInstanceInfo = feishuHttpApi.getApprovalInstanceInfo(eventCallback.getEvent().getInstanceCode()).getData();

                        //获取审批的具体控件内容
                        JsonNode rootNode = objectMapper.readTree(approvalInstanceInfo.getForm());
                        // 申请使用的知识库用户id
                        List<String> openIds = new ArrayList<>();
                        // 设置一个默认的很早的时间
                        String expireTimeNew = "1970-01-01T00:00:00+08:00";
                        for (JsonNode node : rootNode) {
                            String type = node.get("type").asText();
                            // 找到contact控件获取申请使用的知识库用户id
                            if (type != null && type.equals("contact")) {
                                JsonNode openIdsNode = node.get("open_ids");
                                if (openIdsNode != null && openIdsNode.isArray()) {
                                    for (JsonNode openIdNode : openIdsNode) {
                                        openIds.add(openIdNode.asText());
                                    }
                                }
                            } else if (type != null && type.equals("date")) {
                                // 找到data控件获取过期时间
                                expireTimeNew = node.get("value").asText();
                            }

                        }
                        // 发起者的用户id
                        String creator = approvalInstanceInfo.getOpenId();

                        // 初始化要保存的审批信息
                        ApprovalSaved newToSave = new ApprovalSaved();
                        newToSave.setOpenIds(openIds);
                        newToSave.setTimestamp(System.currentTimeMillis());
                        newToSave.setExpireTime(expireTimeNew);

                        // 保存审批信息的redis键
                        String redisKey = "approval:" + creator;

                        // 获取之前的审批信息
                        String value = (String) redisService.get(redisKey);
                        if (value != null && !value.isEmpty()) {
                            // 检查过去存的
                            List<ApprovalSaved> approvalSavedList = objectMapper.readValue(value, new TypeReference<List<ApprovalSaved>>() {
                            });
                            List<ApprovalSaved> savedList = new ArrayList<>();
                            for (ApprovalSaved approvalSaved : approvalSavedList) {
                                String expireStr = approvalSaved.getExpireTime();
                                ZonedDateTime expireDateTime = ZonedDateTime.parse(expireStr, DateTimeFormatter.ISO_ZONED_DATE_TIME).plusDays(1);
                                ZonedDateTime now = ZonedDateTime.now();
                                // 检查过去存着的是否过期
                                if (now.isBefore(expireDateTime)) {
                                    savedList.add(approvalSaved);
                                }
                                // 检查新添加的是否过期
                                ZonedDateTime newToSaveTime = ZonedDateTime.parse(expireTimeNew, DateTimeFormatter.ISO_ZONED_DATE_TIME).plusDays(1);
                                if (now.isBefore(newToSaveTime)) {
                                    savedList.add(approvalSaved);
                                }
                            }
                            redisService.set(redisKey, objectMapper.writeValueAsString(savedList));
                        } else {
                            // 保存新的
                            List<ApprovalSaved> approvalSavedList = new ArrayList<>();
                            approvalSavedList.add(newToSave);
                            redisService.set(redisKey, objectMapper.writeValueAsString(approvalSavedList));
                        }
                    }
                }
            })
            .build();

    /**
     * 启动即监听
     */
    @PostConstruct
    public void startListen() {
        Client cli = new Client.Builder(feishuConfig.getAppId(), feishuConfig.getAppSecret())
                .eventHandler(EVENT_HANDLER)
                .build();
        cli.start();
    }
}
