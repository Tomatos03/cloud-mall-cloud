package com.cloudmall.aigc.service;

import com.cloudmall.aigc.model.resp.ChatMessageResp;
import com.cloudmall.aigc.model.resp.CreateSessionResp;
import com.cloudmall.aigc.model.resp.HotTopicResp;
import com.cloudmall.aigc.model.resp.SessionHistoryItemResp;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取会话标题。
     *
     * @param sessionId 会话 ID
     * @return 会话标题，首次聊天时为 null
     */
    String getSessionTitle(String sessionId);

    /**
     * 更新会话标题。
     *
     * @param sessionId 会话 ID
     * @param title     新标题
     */
    void updateSessionTitle(String sessionId, String title);

    /**
     * 按时间分类获取当前用户的历史会话列表
     *
     * @return 分类名称 -> 会话条目列表（有序）
     */
    Map<String, List<SessionHistoryItemResp>> getSessionHistory();

    /**
     * 删除会话（软删数据库 + 清空 Redis 缓存）
     *
     * @param sessionId 会话 ID
     */
    void deleteSession(String sessionId);

    /**
     * 校验会话是否存在且属于当前用户
     *
     * @param sessionId 会话 ID
     * @throws BizException 会话不存在或不属于当前用户时抛出 SESSION_NOT_FOUND
     */
    void validateSession(String sessionId);

    /**
     * 生成会话在 AI ChatMemory 中的全局唯一标识
     *
     * @param sessionId 会话 ID
     * @return 格式为 {userId}_{sessionId} 的字符串
     */
    String getConversationId(String sessionId);
}
