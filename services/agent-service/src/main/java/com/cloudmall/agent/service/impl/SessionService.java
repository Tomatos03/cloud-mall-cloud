package com.cloudmall.agent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmall.agent.entity.ChatSessionDO;
import com.cloudmall.agent.mapper.ChatSessionMapper;
import com.cloudmall.agent.model.resp.ChatMessageResp;
import com.cloudmall.agent.model.resp.CreateSessionResp;
import com.cloudmall.agent.model.resp.HotTopicResp;
import com.cloudmall.agent.properties.AgentProperties;
import com.cloudmall.agent.service.ISessionService;
import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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
     * 校验会话是否存在且属于当前用户
     *
     * @param sessionId 会话 ID
     * @throws BizException 会话不存在或不属于当前用户时抛出 SESSION_NOT_FOUND
     */
    private void validateSession(String sessionId) {
        ChatSessionDO chatSession = chatSessionMapper.selectOne(
                Wrappers.<ChatSessionDO>lambdaQuery()
                        .eq(ChatSessionDO::getSessionId, sessionId)
                        .eq(ChatSessionDO::getUserId, UserContextHolder.getUserId())
        );
        AssertUtils.notNull(chatSession, BizErrorCode.SESSION_NOT_FOUND);
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

    /**
     * 生成会话在 AI ChatMemory 中的全局唯一标识
     *
     * @param sessionId 会话 ID
     * @return 格式为 {userId}_{sessionId} 的字符串
     */
    private String getConversationId(String sessionId) {
        return String.format("%s_%s", UserContextHolder.getUserId(), sessionId);
    }
}
