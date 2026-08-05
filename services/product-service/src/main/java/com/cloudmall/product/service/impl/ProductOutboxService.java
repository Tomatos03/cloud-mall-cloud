package com.cloudmall.product.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.product.api.event.ProductEvent;
import com.cloudmall.product.api.event.ProductTagEnum;
import com.cloudmall.product.entity.ProductOutboxDO;
import com.cloudmall.product.enums.OutboxPublishStatusEnum;
import com.cloudmall.product.mapper.ProductOutboxMapper;
import com.cloudmall.product.service.IProductOutboxService;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * 商品服务 Outbox 服务实现
 * <p>
 * 商品事件与状态变更在同一本地事务提交，定时任务扫描待发布事件
 * 按 tag 发送 RocketMQ 并更新状态；发送失败按退避重试，超限进入死信。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductOutboxService implements IProductOutboxService {

    /** 商品事件 Topic */
    public static final String PRODUCT_EVENTS_TOPIC = "PRODUCT_EVENTS";

    /** 最大重试次数，超过进入死信 */
    private static final int MAX_RETRY = 10;

    /** 初始重试间隔秒数（指数退避） */
    private static final int RETRY_BASE_SECONDS = 30;

    private final ProductOutboxMapper productOutboxMapper;
    private final MQProducer mqProducer;

    /**
     * 追加商品事件（与商品变更同事务）。
     *
     * @param productId 商品 ID
     * @param spuCode SPU 编码
     * @param eventType 事件类型
     */
    @Override
    public void appendProductEvent(Long productId, String spuCode, ProductTagEnum eventType) {
        String eventId = UUID.randomUUID()
                             .toString()
                             .replace("-", "");
        ProductEvent event = ProductEvent.builder()
                                         .eventId(eventId)
                                         .productId(productId)
                                         .spuCode(spuCode)
                                         .build();
        ProductOutboxDO outbox = ProductOutboxDO.builder()
                                                .eventId(eventId)
                                                .aggregateType("PRODUCT")
                                                .aggregateId(String.valueOf(productId))
                                                .eventType(eventType.getTag())
                                                .payload(JSONUtil.toJsonStr(event))
                                                .publishStatus(OutboxPublishStatusEnum.PENDING)
                                                .retryCount(0)
                                                .build();
        productOutboxMapper.insert(outbox);
    }

    /**
     * 发布一批待发布事件，更新发布状态。
     *
     * @param batchSize 批次大小
     * @return 本次尝试发布条数
     */
    @Override
    public int publishPending(int batchSize) {
        List<ProductOutboxDO> pending = productOutboxMapper.listDuePending(batchSize);
        if (CollUtil.isEmpty(pending)) {
            return 0;
        }
        for (ProductOutboxDO outbox : pending) {
            publishOne(outbox);
        }
        return pending.size();
    }

    /**
     * 发布单条事件：按事件类型发送到对应 tag，成功标记已发布。
     *
     * @param outbox 事件
     */
    private void publishOne(ProductOutboxDO outbox) {
        try {
            ProductEvent event = JSONUtil.toBean(JSONUtil.parseObj(outbox.getPayload()), ProductEvent.class);
            mqProducer.sendSync(PRODUCT_EVENTS_TOPIC, outbox.getEventType(), event);
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
    private void markPublished(ProductOutboxDO outbox) {
        productOutboxMapper.markPublished(outbox.getId());
    }

    /**
     * 记录发布失败：重试次数递增，退避下次重试，超限进入死信。
     *
     * @param outbox 事件
     * @param e 异常
     */
    private void markFailed(ProductOutboxDO outbox, Exception e) {
        int retryCount = outbox.getRetryCount() == null ? 0 : outbox.getRetryCount();
        int nextRetry = retryCount + 1;
        OutboxPublishStatusEnum status = nextRetry >= MAX_RETRY
                ? OutboxPublishStatusEnum.DEAD
                : OutboxPublishStatusEnum.PENDING;
        productOutboxMapper.markFailed(
                outbox.getId(),
                nextRetry,
                LocalDateTime.now().plusSeconds(RETRY_BASE_SECONDS * (long) nextRetry),
                e.getMessage(),
                status
        );
    }
}
