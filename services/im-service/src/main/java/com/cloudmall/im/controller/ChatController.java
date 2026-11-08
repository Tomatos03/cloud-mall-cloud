package com.cloudmall.im.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.common.model.Result;
import com.cloudmall.im.application.IChatAppService;
import com.cloudmall.im.model.req.MessageCursorReq;
import com.cloudmall.im.model.req.SessionPageReq;
import com.cloudmall.im.model.resp.ChatSessionResp;
import com.cloudmall.im.model.resp.CreateSessionResp;
import com.cloudmall.im.model.resp.MessageResp;

/**
 * 聊天控制器
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatAppService chatAppService;

    /**
     * 创建买家与商家的会话（已存在则复用）。
     *
     * @param storeId 店铺ID
     * @return 会话ID
     */
    @PostMapping("/sessions")
    public Result<CreateSessionResp> createChatSession(@RequestParam Long storeId) {
        return Result.success(chatAppService.createChatSession(UserContextHolder.getUserId(), storeId));
    }

    /**
     * 获取当前用户的会话列表。
     *
     * @param req 分页请求
     * @return 会话分页数据
     */
    @GetMapping("/sessions")
    public Result<IPage<ChatSessionResp>> queryChatSession(@Valid SessionPageReq req) {
        return Result.success(chatAppService.pageChatSession(UserContextHolder.getUserId(), req));
    }

    /**
     * 获取指定会话的消息历史（ID 游标分页，第一页为最新消息）。
     *
     * @param sessionId 会话ID
     * @param req 游标查询请求
     * @return 消息游标分页数据
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<CursorResult<MessageResp>> getMessageHistory(
        @PathVariable
        Long sessionId,
        MessageCursorReq req
    ) {
        req.setSessionId(sessionId);
        return Result.success(chatAppService.cursorMessageHistory(UserContextHolder.getUserId(), req));
    }

    /**
     * 标记会话为已读。
     *
     * @param sessionId 会话ID
     * @return 处理结果
     */
    @PutMapping("/sessions/{sessionId}/read")
    public Result<Void> markAsRead(@PathVariable Long sessionId) {
        chatAppService.markReadForChatSession(UserContextHolder.getUserId(), sessionId);
        return Result.success();
    }

    /**
     * 置顶或取消置顶会话。
     *
     * @param sessionId 会话ID
     * @param pinned 是否置顶
     * @return 处理结果
     */
    @PutMapping("/sessions/{sessionId}/pin")
    public Result<Void> setSessionPinned(@PathVariable Long sessionId, @RequestParam Boolean pinned) {
        chatAppService.setSessionPinned(UserContextHolder.getUserId(), sessionId, pinned);
        return Result.success();
    }

    /**
     * 删除当前用户的会话（仅从当前用户列表隐藏）。
     *
     * @param sessionId 会话ID
     * @return 处理结果
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteChatSession(@PathVariable Long sessionId) {
        chatAppService.deleteChatSession(UserContextHolder.getUserId(), sessionId);
        return Result.success();
    }

    /**
     * 批量删除当前用户的会话（仅从当前用户列表隐藏）。
     *
     * @param sessionIds 会话ID列表
     * @return 处理结果
     */
    @DeleteMapping("/sessions")
    public Result<Void> deleteChatSessions(@RequestParam List<Long> sessionIds) {
        chatAppService.deleteChatSessions(UserContextHolder.getUserId(), sessionIds);
        return Result.success();
    }
}
