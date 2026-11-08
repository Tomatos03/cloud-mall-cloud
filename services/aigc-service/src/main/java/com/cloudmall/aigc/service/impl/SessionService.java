package com.cloudmall.aigc.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.aigc.convert.HotTopicConverter;
import com.cloudmall.aigc.enums.SessionTimeBucketEnum;
import com.cloudmall.aigc.mapper.ChatSessionMapper;
import com.cloudmall.aigc.memory.ChatCardStore;
import com.cloudmall.aigc.model.entity.ChatSessionDO;
import com.cloudmall.aigc.model.resp.AgentCardResp;
import com.cloudmall.aigc.model.resp.ChatMessageResp;
import com.cloudmall.aigc.model.resp.CreateSessionResp;
import com.cloudmall.aigc.model.resp.HotTopicResp;
import com.cloudmall.aigc.model.resp.SessionHistoryItemResp;
import com.cloudmall.aigc.properties.AgentProperties;
import com.cloudmall.aigc.service.ISessionService;
import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;

/**
 * 会话管理服务实现
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService implements ISessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final AgentProperties agentProperties;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatCardStore chatCardStore;
    private final HotTopicConverter hotTopicConverter;

    /**
     * 创建新会话 — 生成 sessionId 并返回会话信息和随机热门话题。 <p>
     * 此时会话还没有任何聊天内容，不写入数据库；首次发送消息时才落库。
     *
     * @param topicCount 热门话题条数
     * @return 会话信息（含 sessionId、标题、描述、热门话题列表）
     */
    @Override
    public CreateSessionResp createSession(int topicCount) {
        String sessionId = IdWorker.getIdStr();
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
        List<AgentProperties.HotTopic> selected = RandomUtil.randomEleList(all, count);
        return hotTopicConverter.toRespList(selected);
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
        String conversationId = getConversationId(sessionId);
        List<Message> messages = chatMemoryRepository.findByConversationId(conversationId);
        return messages
                .stream()
                .filter(
                        m -> m.getMessageType() == MessageType.USER
                                     || m.getMessageType() == MessageType.ASSISTANT
                )
                .map(m -> toChatMessageResp(m, conversationId))
                .toList();
    }

    /**
     * 保存助手回复对应的工具卡片。
     *
     * @param conversationId 会话唯一标识
     * @param assistantContent 助手回复内容
     * @param cards 卡片列表
     */
    @Override
    public void saveCards(String conversationId, String assistantContent, List<AgentCardResp> cards) {
        chatCardStore.save(conversationId, assistantContent, cards);
    }

    /**
     * 将聊天记忆消息转换为历史消息响应，助手消息按内容回填卡片。
     *
     * @param message 聊天记忆消息
     * @param conversationId 会话唯一标识
     * @return 历史消息响应
     */
    private ChatMessageResp toChatMessageResp(Message message, String conversationId) {
        String role = message.getMessageType().name();
        String content = message.getText();
        List<AgentCardResp> cards = message.getMessageType() == MessageType.ASSISTANT
                ? chatCardStore.get(conversationId, content)
                : List.of();
        return ChatMessageResp.builder()
                              .role(role)
                              .content(content)
                              .cards(cards.isEmpty() ? null : cards)
                              .build();
    }

    /**
     * 删除会话 — 软删数据库记录 + 清空 Redis 聊天缓存
     *
     * @param sessionId 会话 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(String sessionId) {
        String conversationId = getConversationId(sessionId);
        chatSessionMapper.deleteBySessionIdAndUserId(sessionId, UserContextHolder.getUserId());
        chatMemoryRepository.deleteByConversationId(conversationId);
        chatCardStore.delete(conversationId);
    }

    /**
     * 按时间分类获取当前用户的历史会话列表
     *
     * @return 分类名称 -> 会话条目列表（有序）
     */
    @Override
    public Map<String, List<SessionHistoryItemResp>> getSessionHistory() {
        Long userId = UserContextHolder.getUserId();

        IPage<ChatSessionDO> page = chatSessionMapper.selectRecentByUser(new Page<>(1, 30), userId);
        List<ChatSessionDO> sessions = page.getRecords();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, List<SessionHistoryItemResp>> result = new LinkedHashMap<>();

        for (ChatSessionDO session : sessions) {
            LocalDateTime updateTime = resolveUpdateTime(session, now);
            for (SessionTimeBucketEnum bucket : SessionTimeBucketEnum.values()) {
                if (bucket.matches(updateTime, now)) {
                    result.computeIfAbsent(bucket.getDisplayName(), k -> new ArrayList<>())
                          .add(
                                  SessionHistoryItemResp.builder()
                                                        .sessionId(session.getSessionId())
                                                        .title(session.getTitle())
                                                        .updateTime(updateTime.format(formatter))
                                                        .build()
                          );
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 解析会话展示时间，更新时间缺失时回退到创建时间，再缺失则使用当前时间。
     *
     * @param session 会话实体
     * @param now 当前时间
     * @return 非空的展示时间
     */
    private LocalDateTime resolveUpdateTime(ChatSessionDO session, LocalDateTime now) {
        LocalDateTime time = ObjectUtil.defaultIfNull(session.getUpdateTime(), session.getCreateTime());
        return ObjectUtil.defaultIfNull(time, now);
    }

    /**
     * 首次发送消息时将会话写入数据库；已存在则保留原标题不变。
     *
     * @param sessionId 会话 ID
     * @param title 会话标题（取首条消息内容）
     */
    @Override
    public void ensureSession(String sessionId, String title) {
        Long userId = UserContextHolder.getUserId();
        ChatSessionDO existing = chatSessionMapper.selectBySessionIdAndUserId(sessionId, userId);
        if (existing != null) {
            return;
        }

        ChatSessionDO session = ChatSessionDO.builder()
                                             .sessionId(sessionId)
                                             .userId(userId)
                                             .title(truncateTitle(title))
                                             .build();
        try {
            chatSessionMapper.insert(session);
        } catch (DuplicateKeyException e) {
            log.debug("会话已被并发请求写入，忽略重复创建: sessionId={}", sessionId);
        }
    }

    /**
     * 更新会话标题；会话尚未落库（空会话）时忽略，等待首条消息落库。
     *
     * @param sessionId 会话标识
     * @param title 标题
     */
    @Override
    public void updateSessionTitle(String sessionId, String title) {
        ChatSessionDO session = chatSessionMapper.selectBySessionIdAndUserId(
                sessionId,
                UserContextHolder.getUserId()
        );
        if (session == null) {
            return;
        }

        session.setTitle(truncateTitle(title));
        chatSessionMapper.updateById(session);
    }

    /**
     * 截断会话标题，避免超出数据库字段长度。
     *
     * @param title 原始标题
     * @return 最长 100 个字符的标题
     */
    private String truncateTitle(String title) {
        return title.substring(0, Math.min(100, title.length()));
    }

    /**
     * 获取已校验的会话实体
     *
     * @param sessionId 会话 ID
     * @return 会话实体
     * @throws BizException 会话不存在或不属于当前用户时抛出
     *                      SESSION_NOT_FOUND
     */
    private ChatSessionDO getValidatedSession(String sessionId) {
        ChatSessionDO chatSession = Asserts.requireNonNull(
                chatSessionMapper.selectBySessionIdAndUserId(sessionId, UserContextHolder.getUserId()),
                BizErrorEnum.SESSION_NOT_FOUND
        );
        return chatSession;
    }

    /**
     * 校验会话是否存在且属于当前用户
     *
     * @param sessionId 会话 ID
     * @throws BizException 会话不存在或不属于当前用户时抛出
     *                      SESSION_NOT_FOUND
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

}
