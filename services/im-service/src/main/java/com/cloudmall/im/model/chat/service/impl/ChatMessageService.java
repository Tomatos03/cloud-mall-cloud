package com.cloudmall.im.model.chat.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.im.model.chat.dto.IMPageParamsDTO;
import com.cloudmall.im.model.chat.entity.ChatMessage;
import com.cloudmall.im.model.chat.mapper.ChatMessageMapper;
import com.cloudmall.im.model.chat.service.IChatMessageService;
import com.cloudmall.im.model.chat.vo.MessageVO;
import com.cloudmall.im.security.context.AuthUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消息服务实现
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    /**
     * 分页获取会话历史消息
     */
    @Override
    public IPage<MessageVO> pageMessageHistory(IMPageParamsDTO params) {
        Page<ChatMessage> page = new Page<>(calculateReversePage(params), params.getPageSize());

        IPage<ChatMessage> messagePage = lambdaQuery().eq(ChatMessage::getSessionId, params.getSessionId())
                                                      .orderByAsc(ChatMessage::getCreateTime)
                                                      .page(page);

        return messagePage.convert(
                msg -> MessageVO.builder()
                                .userId(msg.getSenderId())
                                .content(msg.getContent())
                                .type(msg.getType())
                                .time(msg.getCreateTime().toString())
                                .build()
        );
    }

    private Integer calculateReversePage(IMPageParamsDTO params) {
        Long totalCount = lambdaQuery().eq(ChatMessage::getSessionId, params.getSessionId())
                                       .count();
        Integer pageSize = params.getPageSize();
        Long totalPage = (totalCount + pageSize - 1) / pageSize;
        return Math.toIntExact(totalPage - params.getPage() + 1);
    }

    /**
     * 删除过期消息（定时任务调用）
     */
    @Override
    public void deleteExpiredMessages() {
        remove(query().lt("expire_time", LocalDateTime.now()));
    }

    @Override
    public ChatMessage getLastMessage(Long sessionId) {
        return lambdaQuery()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("limit 1")
                .one();
    }

    @Override
    public Long countUnreadMessages(Long sessionId) {
        return lambdaQuery().eq(ChatMessage::getSessionId, sessionId)
                            .eq(ChatMessage::getIsRead, false)
                            .ne(ChatMessage::getSenderId, AuthUserContext.getUserId())
                            .count();
    }
}
