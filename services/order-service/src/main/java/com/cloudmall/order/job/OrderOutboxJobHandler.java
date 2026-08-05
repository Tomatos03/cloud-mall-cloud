package com.cloudmall.order.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.order.service.IOrderOutboxService;

/**
 * 订单服务 Outbox 发布 JobHandler
 * <p>
 * 周期扫描待发布订单事件并发送到 RocketMQ。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxJobHandler {

    /** 单批发布上限 */
    private static final int BATCH_SIZE = 100;

    private final IOrderOutboxService orderOutboxService;

    /**
     * 周期发布待发布事件。
     *
     * @param param 任务参数（未使用）
     */
    @XxlJob("orderOutboxPublish")
    public void publish(String param) {
        int published = orderOutboxService.publishPending(BATCH_SIZE);
        if (published > 0) {
            log.debug("order Outbox 发布 {} 条", published);
        }
    }
}
