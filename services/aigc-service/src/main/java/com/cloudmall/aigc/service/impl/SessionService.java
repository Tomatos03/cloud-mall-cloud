package com.cloudmall.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmall.aigc.entity.ChatSessionDO;
import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.aigc.mapper.ChatSessionMapper;
import com.cloudmall.aigc.model.resp.ChatMessageResp;
import com.cloudmall.aigc.model.resp.CreateSessionResp;
import com.cloudmall.aigc.model.resp.HotTopicResp;
import com.cloudmall.aigc.properties.AgentProperties;
import com.cloudmall.aigc.service.ISessionService;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.aigc.model.enums.SessionTimeBucket;
import com.cloudmall.aigc.model.resp.SessionHistoryItemResp;
import com.cloudmall.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会话管理服务实现
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Service
@RequiredArgsConstructor
public class SessionService implements ISessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final AgentProperties agentProperties;
    private final ChatMemoryRepository chatMemoryRepository;

    /**
     * 创建新会话 — 生成 sessionId、插入数据库、返回会话信息和随机热门话题
     *
     * @param topicCount 热门话题条数
     * @return 会话信息（含 sessionId、标题、描述、热门话题列表）
     */
    @Override
    public CreateSessionResp createSession(int topicCount) {
        String sessionId = newSession();
        List<HotTopicResp> examples = getHotTopics(topicCount);
        return CreateSessionResp.builder()
                .sessionId(sessionId)
                .title(agentProperties.getSession().getTitle())
                .describe(agentProperties.getSession().getDescribe())
                .examples(examples)
                .build();
    }

    /**
     * 从配置的热门话题列表中随机选取指定数量返回
     *
     * @param topicCount 需要返回的话题数量
     * @return 热门话题列表，配置为空时返回空列表
     */
    @Override
    public List<HotTopicResp> getHotTopics(int topicCount) {
        List<AgentProperties.HotTopic> all = agentProperties.getSession().getHotTopics();
        if (CollectionUtil.isEmpty(all)) {
            return Collections.emptyList();
        }

        int count = Math.min(topicCount, all.size());
        return RandomUtil.randomEleList(all, count)
                .stream()
                .map(h -> BeanUtil.toBean(h, HotTopicResp.class))
                .collect(Collectors.toList());
    }

    /**
     * 获取会话中的所有消息（仅 USER 和 ASSISTANT 类型）
     *
     * @param sessionId 会话 ID
     * @return 消息列表，每条包含 role 和 content
     */
    @Override
    public List<ChatMessageResp> getSessionMessages(String sessionId) {
        validateSession(sessionId);
        List<Message> messages = chatMemoryRepository.findByConversationId(getConversationId(sessionId));
        return messages.stream()
                       .filter(m ->
                               m.getMessageType() == MessageType.USER
                               || m.getMessageType() == MessageType.ASSISTANT
                       )
                       .map(m -> ChatMessageResp
                               .builder()
                               .role(m.getMessageType().name())
                               .content(m.getText())
                               .build()
                       )
                       .toList();
    }

    /**
     * 删除会话 — 软删数据库记录 + 清空 Redis 聊天缓存
     *
     * @param sessionId 会话 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(String sessionId) {
        chatSessionMapper.deleteById(sessionId);
        chatMemoryRepository.deleteByConversationId(getConversationId(sessionId));
    }

    /**
     * 按时间分类获取当前用户的历史会话列表
     *
     * @return 分类名称 -> 会话条目列表（有序）
     */
    @Override
    public Map<String, List<SessionHistoryItemResp>> getSessionHistory() {
        Long userId = UserContextHolder.getUserId();

        List<ChatSessionDO> sessions = chatSessionMapper.selectList(
                Wrappers.<ChatSessionDO>lambdaQuery()
                        .eq(ChatSessionDO::getUserId, userId)
                        .orderByDesc(ChatSessionDO::getUpdateTime)
                        .last(LimitEnum.THIRTY.getSql())
        );

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, List<SessionHistoryItemResp>> result = new LinkedHashMap<>();

        for (ChatSessionDO session : sessions) {
            for (SessionTimeBucket bucket : SessionTimeBucket.values()) {
                if (bucket.matches(session.getUpdateTime(), now)) {
                    result.computeIfAbsent(bucket.getDisplayName(), k -> new ArrayList<>())
                          .add(SessionHistoryItemResp.builder()
                                  .sessionId(session.getSessionId())
                                  .title(session.getTitle())
                                  .updateTime(session.getUpdateTime().format(formatter))
                                  .build()
                          );
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 更新会话标题
     *
     * @param sessionId 会话 ID
     * @param title     新标题
     */
    @Override
    public String getSessionTitle(String sessionId) {
        ChatSessionDO session = getValidatedSession(sessionId);
        return session.getTitle();
    }

    @Override
    public void updateSessionTitle(String sessionId, String title) {
        ChatSessionDO session = getValidatedSession(sessionId);
        int titleLen = Math.min(100, title.length());
        session.setTitle(title.substring(0, titleLen));
        chatSessionMapper.updateById(session);
    }

    /**
     * 获取已校验的会话实体
     *
     * @param sessionId 会话 ID
     * @return 会话实体
     * @throws BizException 会话不存在或不属于当前用户时抛出 SESSION_NOT_FOUND
     */
    private ChatSessionDO getValidatedSession(String sessionId) {
        ChatSessionDO chatSession = chatSessionMapper.selectOne(
                Wrappers.<ChatSessionDO>lambdaQuery()
                        .eq(ChatSessionDO::getSessionId, sessionId)
                        .eq(ChatSessionDO::getUserId, UserContextHolder.getUserId())
        );
        AssertUtils.notNull(chatSession, BizErrorCode.SESSION_NOT_FOUND);
        return chatSession;
    }

    /**
     * 校验会话是否存在且属于当前用户
     *
     * @param sessionId 会话 ID
     * @throws BizException 会话不存在或不属于当前用户时抛出 SESSION_NOT_FOUND
     */
    @Override
    public void validateSession(String sessionId) {
        getValidatedSession(sessionId);
    }

    /**
     * 生成会话在 AI ChatMemory 中的全局唯一标识
     *
     * @param sessionId 会话 ID
     * @return 格式为 {userId}_{sessionId} 的字符串
     */
    @Override
    public String getConversationId(String sessionId) {
        return String.format("%s_%s", UserContextHolder.getUserId(), sessionId);
    }

    /**
     * 创建新会话记录并返回 sessionId
     *
     * @return 新生成的 sessionId
     */
    private String newSession() {
        Long userId = UserContextHolder.getUserId();
        String sessionId = IdWorker.getIdStr();

        ChatSessionDO session = ChatSessionDO.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title("New Session")
                .build();
        chatSessionMapper.insert(session);
        return sessionId;
    }

}
