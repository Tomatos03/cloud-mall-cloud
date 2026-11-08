package com.cloudmall.aigc.memory;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.cloudmall.aigc.model.resp.AgentCardResp;

/**
 * 会话卡片存储。
 * <p>
 * 工具卡片不属于 Spring AI 的聊天消息内容，单独按会话和助手回复内容哈希存入 Redis，
 * 历史消息恢复时按内容回填卡片。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCardStore {

    private static final String KEY_PREFIX = "chatcard:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 保存某条助手回复对应的卡片。
     *
     * @param conversationId 会话唯一标识
     * @param assistantContent 助手回复内容
     * @param cards 卡片列表
     */
    public void save(String conversationId, String assistantContent, List<AgentCardResp> cards) {
        if (StrUtil.isBlank(conversationId) || CollUtil.isEmpty(cards)) {
            return;
        }
        try {
            stringRedisTemplate.opsForHash()
                               .put(
                                       buildKey(conversationId),
                                       digest(assistantContent),
                                       JSONUtil.toJsonStr(cards)
                               );
        } catch (Exception e) {
            log.warn("保存会话卡片失败: conversationId={}", conversationId, e);
        }
    }

    /**
     * 读取某条助手回复对应的卡片。
     *
     * @param conversationId 会话唯一标识
     * @param assistantContent 助手回复内容
     * @return 卡片列表，未找到时返回空列表
     */
    public List<AgentCardResp> get(String conversationId, String assistantContent) {
        if (StrUtil.isBlank(conversationId)) {
            return List.of();
        }
        try {
            Object json = stringRedisTemplate.opsForHash()
                                             .get(buildKey(conversationId), digest(assistantContent));
            if (json == null) {
                return List.of();
            }
            return JSONUtil.toList(json.toString(), AgentCardResp.class);
        } catch (Exception e) {
            log.warn("读取会话卡片失败: conversationId={}", conversationId, e);
            return List.of();
        }
    }

    /**
     * 删除会话的全部卡片。
     *
     * @param conversationId 会话唯一标识
     */
    public void delete(String conversationId) {
        if (StrUtil.isBlank(conversationId)) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildKey(conversationId));
        } catch (Exception e) {
            log.warn("删除会话卡片失败: conversationId={}", conversationId, e);
        }
    }

    private String digest(String assistantContent) {
        return DigestUtil.sha256Hex(assistantContent == null ? "" : assistantContent);
    }

    private String buildKey(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}
