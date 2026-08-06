package com.cloudmall.outbox.appender;

import java.util.UUID;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;

import com.cloudmall.outbox.entity.OutboxDO;
import com.cloudmall.outbox.enums.OutboxPublishStatusEnum;
import com.cloudmall.outbox.mapper.OutboxMapper;
import com.cloudmall.outbox.model.OutboxAppendReq;

/**
 * Outbox 事件追加器
 * <p>
 * 业务服务在本地事务内调用 {@link #append}，事件与业务写入同事务提交。
 * 事件对象必须包含 eventId 字段（消费端幂等键），由本追加器生成并注入，
 * 保证行内 event_id 与 payload 内的 eventId 始终一致。
 *
 * @author : Tomatos
 */
@RequiredArgsConstructor
public class OutboxAppender {

    private final OutboxMapper outboxMapper;

    /** 来源服务（spring.application.name） */
    private final String appName;

    /**
     * 追加一条待发布事件（与业务写入同事务提交）。
     *
     * @param req 追加请求
     */
    public void append(OutboxAppendReq req) {
        String eventId = UUID.randomUUID()
                             .toString()
                             .replace("-", "");
        BeanUtil.setFieldValue(req.getEvent(), "eventId", eventId);
        OutboxDO outbox = OutboxDO.builder()
                                  .appName(appName)
                                  .eventId(eventId)
                                  .topic(req.getTopic())
                                  .eventType(req.getEventType())
                                  .bizType(req.getBizType())
                                  .bizId(req.getBizId())
                                  .payload(JSONUtil.toJsonStr(req.getEvent()))
                                  .publishStatus(OutboxPublishStatusEnum.PENDING)
                                  .retryCount(0)
                                  .build();
        outboxMapper.insert(outbox);
    }
}
