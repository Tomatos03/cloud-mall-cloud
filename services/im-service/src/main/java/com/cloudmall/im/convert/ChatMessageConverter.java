package com.cloudmall.im.convert;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.im.api.model.event.ImMessagePushEvent;
import com.cloudmall.im.api.model.resp.MessageResp;
import com.cloudmall.im.model.entity.ChatMessageDO;

/**
 * 聊天消息转换器
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Mapper(componentModel = "spring")
public interface ChatMessageConverter {

    /**
     * 将消息实体转换为响应对象。
     *
     * @param message 消息实体
     * @return 响应对象
     */
    @Mapping(target = "time", source = "createTime")
    @Mapping(target = "userId", source = "senderId")
    MessageResp toResp(ChatMessageDO message);

    /**
     * 将消息实体列表转换为响应对象列表。
     *
     * @param messages 消息实体列表
     * @return 响应对象列表
     */
    List<MessageResp> toRespList(List<ChatMessageDO> messages);

    /**
     * 将消息实体转换为推送事件。
     *
     * @param message 消息实体
     * @param receiverId 接收者 ID
     * @return 推送事件
     */
    @Mapping(target = "messageId", source = "id")
    @Mapping(target = "createdAt", source = "createTime")
    @Mapping(target = "receiverId", expression = "java(receiverId)")
    ImMessagePushEvent toPushEvent(ChatMessageDO message, @Context Long receiverId);

    /**
     * 将推送事件转换为消息视图。
     *
     * @param event 推送事件
     * @return 消息视图
     */
    @Mapping(target = "id", source = "messageId")
    @Mapping(target = "time", source = "createdAt")
    @Mapping(target = "userId", source = "senderId")
    MessageResp toResp(ImMessagePushEvent event);
}
