package com.cloudmall.outbox.appender;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;

import com.cloudmall.outbox.enums.OutboxStatusEnum;
import com.cloudmall.outbox.mapper.OutboxMapper;
import com.cloudmall.outbox.model.entity.OutboxDO;

/**
 * Outbox 事件写入器。
 * <p>
 * 使用固定表 {@code cm_message_outbox}，业务代码不感知表结构。
 * 仅在切面中调用，不对外暴露。
 *
 * @author : Tomatos
 */
@RequiredArgsConstructor
public class OutboxAppender {

    private final OutboxMapper outboxMapper;

    /** 来源服务（spring.application.name） */
    private final String appName;

    /**
     * 写入一条待处理事件。
     *
     * @param eventType 事件类型
     * @param bizId 业务 ID
     * @param payload 事件载荷，字符串原样写入，其余序列化为 JSON
     */
    public void append(String eventType, String bizId, Object payload) {
        outboxMapper.insert(
                OutboxDO.builder()
                        .appName(appName)
                        .eventType(eventType)
                        .bizId(bizId)
                        .payload(serialize(payload))
                        .status(OutboxStatusEnum.PENDING)
                        .retryCount(0)
                        .build()
        );
    }

    /**
     * 序列化载荷：字符串原样写入，其余转为 JSON。
     *
     * @param payload 载荷
     * @return 待写入的文本
     */
    private static String serialize(Object payload) {
        if (payload == null) {
            return null;
        }
        return payload instanceof CharSequence text ? text.toString() : JSONUtil.toJsonStr(payload);
    }
}
