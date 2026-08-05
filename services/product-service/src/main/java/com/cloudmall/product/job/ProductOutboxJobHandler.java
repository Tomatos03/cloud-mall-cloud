package com.cloudmall.product.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.product.service.IProductOutboxService;

/**
 * 商品服务 Outbox 发布 JobHandler
 * <p>
 * 周期扫描待发布商品事件并发送到 RocketMQ。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOutboxJobHandler {

    /** 单批发布上限 */
    private static final int BATCH_SIZE = 100;

    private final IProductOutboxService productOutboxService;

    /**
     * 周期发布待发布事件。
     *
     * @param param 任务参数（未使用）
     */
    @XxlJob("productOutboxPublish")
    public void publish(String param) {
        int published = productOutboxService.publishPending(BATCH_SIZE);
        if (published > 0) {
            log.debug("product Outbox 发布 {} 条", published);
        }
    }
}
