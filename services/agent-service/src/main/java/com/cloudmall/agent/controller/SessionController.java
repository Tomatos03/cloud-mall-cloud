package com.cloudmall.agent.controller;

import com.cloudmall.agent.model.resp.ChatMessageResp;
import com.cloudmall.agent.model.resp.CreateSessionResp;
import com.cloudmall.agent.model.resp.HotTopicResp;
import com.cloudmall.agent.service.ISessionService;
import com.cloudmall.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
