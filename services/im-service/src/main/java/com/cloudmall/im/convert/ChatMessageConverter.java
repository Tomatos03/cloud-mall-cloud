package com.cloudmall.im.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.im.entity.ChatMessageDO;
import com.cloudmall.im.model.resp.MessageResp;

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
}
