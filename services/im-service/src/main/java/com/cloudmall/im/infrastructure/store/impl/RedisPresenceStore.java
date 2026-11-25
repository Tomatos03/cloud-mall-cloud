package com.cloudmall.im.infrastructure.store.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import com.cloudmall.im.constant.ImRedisConstants;
import com.cloudmall.im.infrastructure.store.IPresenceStore;
import com.cloudmall.im.model.bo.UserRoute;
import com.cloudmall.im.properties.ImProperties;

/**
 * Redis 在线路由存储实现。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Component
@RequiredArgsConstructor
public class RedisPresenceStore implements IPresenceStore {

    private static final RedisScript<Long> DELETE_ROUTE_IF_MATCH = RedisScript.of(
            new ClassPathResource("lua/delete_route_if_match.lua"),
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final ImProperties properties;

    @Override
    public void register(Long userId, UserRoute route) {
        stringRedisTemplate.opsForHash().put(key(userId), route.sessionId(), encode(route));
        expire(userId);
    }

    @Override
    public void refresh(Long userId, UserRoute route) {
        stringRedisTemplate.opsForHash().put(key(userId), route.sessionId(), encode(route));
        expire(userId);
    }

    @Override
    public List<UserRoute> routes(Long userId) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key(userId));
        return CollStreamUtil.toList(
                entries.entrySet(),
                entry -> decode(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()))
        );
    }

    @Override
    public void unregisterIfMatch(Long userId, UserRoute route) {
        stringRedisTemplate.execute(
                DELETE_ROUTE_IF_MATCH,
                List.of(key(userId)),
                route.sessionId(),
                route.instanceId()
        );
    }

    private void expire(Long userId) {
        stringRedisTemplate.expire(key(userId), Duration.ofSeconds(properties.getPresenceTtlSeconds()));
    }

    private static String encode(UserRoute route) {
        return JSONUtil.createObj()
                       .set("instanceId", route.instanceId())
                       .set("lastHeartbeat", route.lastHeartbeat())
                       .toString();
    }

    private static UserRoute decode(String sessionId, String value) {
        var json = JSONUtil.parseObj(value);
        return new UserRoute(json.getStr("instanceId"), sessionId, json.getLong("lastHeartbeat", 0L));
    }

    private static String key(Long userId) {
        return ImRedisConstants.ROUTE_PREFIX + userId;
    }
}
