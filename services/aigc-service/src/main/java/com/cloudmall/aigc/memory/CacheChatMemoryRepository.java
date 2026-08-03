package com.cloudmall.aigc.memory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Slf4j
@RequiredArgsConstructor
public class CacheChatMemoryRepository implements ChatMemoryRepository {
    private final static String KEY_PREFIX = "chat:";
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 查找符合条件的数据。
     *
     * @return 数据列表
     */
    @Override
    public List<String> findConversationIds() {
        Set<String> keys;
        try (Cursor<String> cursor = stringRedisTemplate.scan(
                ScanOptions.scanOptions()
                           .match(KEY_PREFIX + "*")
                           .count(200)
                           .build()
        )) {
            keys = cursor.stream().collect(Collectors.toSet());
        }
        return keys.stream()
                   .map(key -> key.replace(KEY_PREFIX, ""))
                   .toList();
    }

    /**
     * 查找符合条件的数据。
     *
     * @param conversationId 会话标识
     * @return 数据列表
     */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<String> messageJsons = stringRedisTemplate.boundListOps(buildKey(conversationId))
                                                       .range(0, -1);
        if (CollectionUtil.isEmpty(messageJsons)) {
            return Collections.emptyList();
        }

        return messageJsons.stream()
                           .map(dto -> JSONUtil.toBean(dto, ChatMessageDTO.class))
                           .map(ChatMessageDTO::toMessage)
                           .toList();
    }

    /**
     * 保存数据。
     *
     * @param conversationId 会话标识
     * @param messages 消息列表
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        log.debug("保存会话消息到 Redis, conversationId: {}, messages: {}", conversationId, messages);
        if (CollectionUtil.isEmpty(messages)) {
            return;
        }
        ChatMessageDTO newMessage = ChatMessageDTO.fromMessage(messages.get(messages.size() - 1));
        stringRedisTemplate.boundListOps(buildKey(conversationId))
                           .rightPush(JSONUtil.toJsonStr(newMessage));
    }

    /**
     * 删除相关数据。
     *
     * @param conversationId 会话标识
     */
    @Override
    public void deleteByConversationId(String conversationId) {
        stringRedisTemplate.delete(buildKey(conversationId));
    }

    /**
     * 构建所需对象。
     *
     * @param conversationId 会话标识
     * @return 字符串结果
     */
    private String buildKey(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}
