package com.cloudmall.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.model.CursorResult;
import com.cloudmall.im.convert.ChatMessageConverter;
import com.cloudmall.im.entity.ChatMessageDO;
import com.cloudmall.im.mapper.ChatMessageMapper;
import com.cloudmall.im.model.req.MessageCursorReq;
import com.cloudmall.im.model.resp.MessageResp;
import com.cloudmall.im.service.IChatMessageService;

/**
 * 聊天消息服务实现
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessageDO> implements IChatMessageService {

    private final ChatMessageConverter messageConverter;

    @Override
    public CursorResult<MessageResp> cursorMessageHistory(MessageCursorReq req) {
        CursorResult<ChatMessageDO> result = baseMapper.cursorHistory(req);
        return CursorResult.<MessageResp>builder()
                           .items(messageConverter.toRespList(result.getItems()))
                           .cursor(result.getCursor())
                           .cursorTime(result.getCursorTime())
                           .hasMore(result.getHasMore())
                           .build();
    }

    @Override
    public ChatMessageDO getLastMessage(Long sessionId) {
        return baseMapper.selectLastMessage(sessionId);
    }

    @Override
    public Long countUnread(Long sessionId, Long currentUserId) {
        return baseMapper.countUnread(sessionId, currentUserId);
    }

    @Override
    public int markRead(Long sessionId, Long excludeSenderId) {
        return baseMapper.markRead(sessionId, excludeSenderId);
    }
}
