package com.cloudmall.product.service;

import com.cloudmall.product.api.event.ProductTagEnum;

/**
 * 商品服务 Outbox 服务
 * <p>
 * 商品变更与事件写入同一本地事务提交，由定时任务发布到 RocketMQ。
 *
 * @author : Tomatos
 */
public interface IProductOutboxService {

    /**
     * 追加商品事件（与商品变更同事务）。
     *
     * @param productId 商品 ID
     * @param spuCode SPU 编码
     * @param eventType 事件类型
     */
    void appendProductEvent(Long productId, String spuCode, ProductTagEnum eventType);

    /**
     * 发布一批待发布事件，更新发布状态。
     *
     * @param batchSize 批次大小
     * @return 本次尝试发布条数
     */
    int publishPending(int batchSize);
}
