package com.cloudmall.aigc.controller;

import com.cloudmall.aigc.model.req.SessionUpdateReq;
import com.cloudmall.aigc.model.resp.ChatMessageResp;
import com.cloudmall.aigc.model.resp.CreateSessionResp;
import com.cloudmall.aigc.model.resp.HotTopicResp;
import com.cloudmall.aigc.model.resp.SessionHistoryItemResp;
import com.cloudmall.aigc.service.ISessionService;
import com.cloudmall.common.entity.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 会话管理接口 — 用户会话创建、热门话题查询、历史消息查看
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ISessionService sessionService;

    /**
     * 创建新会话，同时返回热门话题列表
     *
     * @param topicCount 热门话题条数（默认 3）
     * @return 会话信息（含 sessionId、标题、描述、热门话题列表）
     */
    @PostMapping
    public Result<CreateSessionResp> createSession(@RequestParam(defaultValue = "3") int topicCount) {
        return Result.success(sessionService.createSession(topicCount));
    }

    /**
     * 获取热门话题示例
     *
     * @param topicCount 返回条数（默认 3）
     * @return 热门话题列表
     */
    @GetMapping("/hot-topics")
    public Result<List<HotTopicResp>> getHotTopics(@RequestParam(defaultValue = "3") int topicCount) {
        return Result.success(sessionService.getHotTopics(topicCount));
    }

    /**
     * 查询指定会话中的所有消息
     *
     * @param sessionId 会话 ID
     * @return 消息列表（仅 USER 和 ASSISTANT 类型）
     */
    @GetMapping("/{sessionId}/messages")
    public Result<List<ChatMessageResp>> getSessionMessages(@PathVariable String sessionId) {
        return Result.success(sessionService.getSessionMessages(sessionId));
    }

    /**
     * 按时间分类获取当前用户的历史会话列表
     *
     * @return 分类名称 -> 会话条目列表
     */
    @GetMapping("/history")
    public Result<Map<String, List<SessionHistoryItemResp>>> getSessionHistory() {
        return Result.success(sessionService.getSessionHistory());
    }

    /**
     * 删除会话（软删 + 清空 Redis 缓存）
     *
     * @param sessionId 会话 ID
     * @return 操作结果
     */
    @DeleteMapping("/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        sessionService.deleteSession(sessionId);
        return Result.success();
    }

    /**
     * 更新会话信息（如标题）
     *
     * @param sessionId 会话 ID
     * @param req       更新内容
     * @return 操作结果
     */
    @PatchMapping("/{sessionId}")
    public Result<Void> updateSession(
        @PathVariable String sessionId,
        @Valid @RequestBody SessionUpdateReq req
    ) {
        sessionService.updateSessionTitle(sessionId, req.getTitle());
        return Result.success();
    }
}
