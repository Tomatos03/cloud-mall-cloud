package com.cloudmall.agent.service;

import com.cloudmall.agent.model.resp.ChatMessageResp;
import com.cloudmall.agent.model.resp.CreateSessionResp;
import com.cloudmall.agent.model.resp.HotTopicResp;

import java.util.List;

/**
 * 会话管理服务接口
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
public interface ISessionService {

    /**
     * 创建新会话，同时返回热门话题列表
     *
     * @param topicCount 热门话题条数
     * @return 会话信息（含 sessionId、会话标题、描述、热门话题列表）
     */
    CreateSessionResp createSession(int topicCount);

    /**
     * 从配置中随机获取热门话题
     *
     * @param topicCount 返回条数
     * @return 热门话题列表
     */
    List<HotTopicResp> getHotTopics(int topicCount);

    /**
     * 获取会话中的所有消息
     *
     * @param sessionId 会话 ID
     * @return 消息列表（仅 USER 和 ASSISTANT 类型）
     */
    List<ChatMessageResp> getSessionMessages(String sessionId);
}
