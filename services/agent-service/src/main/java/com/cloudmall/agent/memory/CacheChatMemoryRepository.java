package com.cloudmall.agent.memory;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@RequiredArgsConstructor
public class CacheChatMemoryRepository implements ChatMemoryRepository {
    private final static String KEY_PREFIX = "chat:";
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<String> findConversationIds() {
        Set<String> keys;
        try (
                Cursor<String> cursor = stringRedisTemplate.scan(
                        ScanOptions.scanOptions()
                                   .match(KEY_PREFIX + "*")
                                   .count(200)
                                   .build()
                )
        ) {
            keys = cursor.stream()
                         .collect(Collectors.toSet());
        }
        return keys.stream()
                   .map(key -> key.replace(KEY_PREFIX, ""))
                   .toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<String> messageJsons = stringRedisTemplate
                .boundListOps(buildKey(conversationId))
                .range(0, -1);
        if (CollectionUtil.isEmpty(messageJsons)) {
            return Collections.emptyList();
        }

        return messageJsons.stream()
                           .map(dto -> JSONUtil.toBean(dto, ChatMessageDTO.class))
                           .map(ChatMessageDTO::toMessage)
                           .toList();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        if (CollectionUtil.isEmpty(messages)) {
            return;
        }
        ChatMessageDTO newMessage = ChatMessageDTO.fromMessage(messages.get(messages.size() - 1));
        stringRedisTemplate.boundListOps(buildKey(conversationId))
                           .rightPush(JSONUtil.toJsonStr(newMessage));
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        stringRedisTemplate.delete(buildKey(conversationId));
    }

    private String buildKey(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}
