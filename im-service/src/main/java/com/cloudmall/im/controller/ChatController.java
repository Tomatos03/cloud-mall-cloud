package com.cloudmall.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.im.common.entity.PageParamsDTO;
import com.cloudmall.im.model.chat.application.IChatAppService;
import com.cloudmall.im.model.chat.dto.CreateSessionResult;
import com.cloudmall.im.model.chat.dto.IMPageParamsDTO;
import com.cloudmall.im.model.chat.service.IChatMessageService;
import com.cloudmall.im.model.chat.vo.ChatSessionVO;
import com.cloudmall.im.model.chat.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final IChatMessageService chatMessageService;
    private final IChatAppService chatAppService;

    @PostMapping("/session/create")
    public CreateSessionResult createChatSession(@RequestParam Long storeId) {
        return chatAppService.createChatSession(storeId);
    }

    /**
     * 获取当前用户的所有会话列表
     */
    @GetMapping("/sessions")
    public IPage<ChatSessionVO> queryChatSession(@Valid PageParamsDTO pageParams) {
        return chatAppService.pageChatSession(pageParams);
    }

    /**
     * 获取指定会话的消息历史
     */
    @GetMapping("/history")
    public IPage<MessageVO> getMessageHistory(@Valid IMPageParamsDTO params) {
        return chatMessageService.pageMessageHistory(params);
    }

    /**
     * 标记会话为已读
     */
    @PutMapping("/read/{sessionId}")
    public void markAsRead(@PathVariable Long sessionId) {
        chatAppService.markReadForChatSession(sessionId);
    }
}
