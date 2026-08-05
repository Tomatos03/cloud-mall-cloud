package com.cloudmall.im.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.im.convert.ChatMessageConverter;
import com.cloudmall.im.entity.ChatMessageDO;
import com.cloudmall.im.mapper.ChatMessageMapper;
import com.cloudmall.im.model.req.MessagePageReq;
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
    public IPage<MessageResp> pageMessageHistory(MessagePageReq req) {
        Long sessionId = req.getSessionId();
        int pageSize = req.getPageSize();
        long total = lambdaQuery().eq(ChatMessageDO::getSessionId, sessionId)
                                  .count();
        long totalPage = Math.max((total + pageSize - 1) / pageSize, 1);
        long reversePage = Math.max(totalPage - req.getPage() + 1, 1);

        IPage<ChatMessageDO> messagePage = lambdaQuery().eq(ChatMessageDO::getSessionId, sessionId)
                                                        .orderByAsc(ChatMessageDO::getCreateTime)
                                                        .page(new Page<>(reversePage, pageSize));
        return messagePage.convert(messageConverter::toResp);
    }

    @Override
    public ChatMessageDO getLastMessage(Long sessionId) {
        IPage<ChatMessageDO> page = lambdaQuery().eq(ChatMessageDO::getSessionId, sessionId)
                                                 .orderByDesc(ChatMessageDO::getCreateTime)
                                                 .page(new Page<>(1, 1));
        return CollUtil.getFirst(page.getRecords());
    }

    @Override
    public Long countUnread(Long sessionId, Long currentUserId) {
        return lambdaQuery().eq(ChatMessageDO::getSessionId, sessionId)
                            .eq(ChatMessageDO::getIsRead, false)
                            .ne(ChatMessageDO::getSenderId, currentUserId)
                            .count();
    }
}
