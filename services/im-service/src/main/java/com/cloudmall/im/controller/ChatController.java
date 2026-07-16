package com.cloudmall.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.entity.Result;
import com.cloudmall.im.common.entity.PageParamsDTO;
import com.cloudmall.im.model.chat.application.IChatAppService;
import com.cloudmall.im.model.chat.dto.CreateSessionResult;
import com.cloudmall.im.model.chat.dto.IMPageParamsDTO;
import com.cloudmall.im.model.chat.service.IChatMessageService;
import com.cloudmall.im.model.chat.vo.ChatSessionVO;
import com.cloudmall.im.model.chat.vo.MessageVO;

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
    public Result<CreateSessionResult> createChatSession(@RequestParam Long storeId) {
        return Result.success(chatAppService.createChatSession(storeId));
    }

    /**
     * 获取当前用户的所有会话列表
     */
    @GetMapping("/sessions")
    public Result<IPage<ChatSessionVO>> queryChatSession(@Valid PageParamsDTO pageParams) {
        return Result.success(chatAppService.pageChatSession(pageParams));
    }

    /**
     * 获取指定会话的消息历史
     */
    @GetMapping("/history")
    public Result<IPage<MessageVO>> getMessageHistory(@Valid IMPageParamsDTO params) {
        return Result.success(chatMessageService.pageMessageHistory(params));
    }

    /**
     * 标记会话为已读
     */
    @PutMapping("/read/{sessionId}")
    public Result<Void> markAsRead(@PathVariable Long sessionId) {
        chatAppService.markReadForChatSession(sessionId);
        return Result.success(null);
    }
}
