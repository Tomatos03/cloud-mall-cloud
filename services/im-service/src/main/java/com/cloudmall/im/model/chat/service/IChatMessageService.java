package com.cloudmall.im.model.chat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.im.model.chat.dto.IMPageParamsDTO;
import com.cloudmall.im.model.chat.entity.ChatMessage;
import com.cloudmall.im.model.chat.vo.MessageVO;

/**
 * 消息服务接口
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
public interface IChatMessageService extends IService<ChatMessage> {
    /**
     * 分页获取会话历史消息
     */
    IPage<MessageVO> pageMessageHistory(IMPageParamsDTO params);

    /**
     * 删除过期消息
     */
    void deleteExpiredMessages();

    ChatMessage getLastMessage(Long sessionId);

    Long countUnreadMessages(Long sessionId);
}
