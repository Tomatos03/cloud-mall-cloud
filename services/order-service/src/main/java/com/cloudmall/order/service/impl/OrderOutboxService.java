package com.cloudmall.order.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.order.api.event.OrderEvent;
import com.cloudmall.order.api.event.OrderTagEnum;
import com.cloudmall.order.entity.OrderOutboxDO;
import com.cloudmall.order.enums.OutboxPublishStatusEnum;
import com.cloudmall.order.mapper.OrderOutboxMapper;
import com.cloudmall.order.service.IOrderOutboxService;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * 订单服务 Outbox 服务实现
 * <p>
 * 订单事件与状态变更在同一本地事务提交，定时任务扫描待发布事件
 * 按 tag 发送 RocketMQ 并更新状态；发送失败按退避重试，超限进入死信。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOutboxService implements IOrderOutboxService {

    /** 订单事件 Topic */
    public static final String ORDER_EVENTS_TOPIC = "ORDER_EVENTS";

    /** 最大重试次数，超过进入死信 */
    private static final int MAX_RETRY = 10;

    /** 初始重试间隔秒数（指数退避） */
    private static final int RETRY_BASE_SECONDS = 30;

    private final OrderOutboxMapper orderOutboxMapper;
    private final MQProducer mqProducer;

    /**
     * 追加订单事件（与状态变更同事务）。
     *
     * @param orderNo 父订单号
     * @param userId 用户 ID
     * @param eventType 事件类型
     * @param refundId 退款单 ID（退款事件）
     */
    @Override
    public void appendOrderEvent(String orderNo, Long userId, OrderTagEnum eventType, String refundId) {
        String eventId = UUID.randomUUID()
                             .toString()
                             .replace("-", "");
        OrderEvent event = OrderEvent.builder()
                                     .eventId(eventId)
                                     .orderNo(orderNo)
                                     .userId(userId)
                                     .refundId(refundId)
                                     .build();
        OrderOutboxDO outbox = OrderOutboxDO.builder()
                                            .eventId(eventId)
                                            .aggregateType("ORDER")
                                            .aggregateId(orderNo)
                                            .eventType(eventType.getTag())
                                            .payload(JSONUtil.toJsonStr(event))
                                            .publishStatus(OutboxPublishStatusEnum.PENDING)
                                            .retryCount(0)
                                            .build();
        orderOutboxMapper.insert(outbox);
    }

    /**
     * 发布一批待发布事件，更新发布状态。
     *
     * @param batchSize 批次大小
     * @return 本次尝试发布条数
     */
    @Override
    public int publishPending(int batchSize) {
        List<OrderOutboxDO> pending = orderOutboxMapper.listDuePending(new Page<>(1, batchSize)).getRecords();
        if (CollUtil.isEmpty(pending)) {
            return 0;
        }
        for (OrderOutboxDO outbox : pending) {
            publishOne(outbox);
        }
        return pending.size();
    }

    /**
     * 发布单条事件：按事件类型发送到对应 tag，成功标记已发布。
     *
     * @param outbox 事件
     */
    private void publishOne(OrderOutboxDO outbox) {
        try {
            OrderEvent event = JSONUtil.toBean(JSONUtil.parseObj(outbox.getPayload()), OrderEvent.class);
            mqProducer.sendSync(ORDER_EVENTS_TOPIC, outbox.getEventType(), event);
            markPublished(outbox);
        } catch (Exception e) {
            log.warn("Outbox 发布失败 eventId={}", outbox.getEventId(), e);
            markFailed(outbox, e);
        }
    }

    /**
     * 标记事件已发布。
     *
     * @param outbox 事件
     */
    private void markPublished(OrderOutboxDO outbox) {
        orderOutboxMapper.markPublished(outbox.getId());
    }

    /**
     * 记录发布失败：重试次数递增，退避下次重试，超限进入死信。
     *
     * @param outbox 事件
     * @param e 异常
     */
    private void markFailed(OrderOutboxDO outbox, Exception e) {
        int retryCount = outbox.getRetryCount() == null ? 0 : outbox.getRetryCount();
        int nextRetry = retryCount + 1;
        OutboxPublishStatusEnum status = nextRetry >= MAX_RETRY
                ? OutboxPublishStatusEnum.DEAD
                : OutboxPublishStatusEnum.PENDING;
        orderOutboxMapper.markFailed(
                outbox.getId(),
                nextRetry,
                LocalDateTime.now().plusSeconds(RETRY_BASE_SECONDS * (long) nextRetry),
                e.getMessage(),
                status
        );
    }
}
