package com.cloudmall.consumer.handler;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.event.ProductEvent;
import com.cloudmall.product.api.event.ProductTagEnum;
import com.cloudmall.product.api.response.ProductDocResp;
import com.cloudmall.rocketmq.handler.EventHandler;
import com.cloudmall.search.api.client.InternalSearchClient;

/**
 * 商品创建/更新处理器：拉取完整搜索文档并写入 ES。
 * <p>
 * 文档组装唯一属于 product-service，本处理器经 ProductClient 拉取；
 * ES 写入经 InternalSearchClient 委托给 search-service；
 * 若商品已不存在（先删除后消费），则清理 ES 残留文档，保证最终一致。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUpsertHandler implements EventHandler<ProductEvent> {

    private final ProductClient productClient;
    private final InternalSearchClient internalSearchClient;

    /**
     * 本处理器服务的 RocketMQ tag 集合（创建/更新共用写入动作）。
     *
     * @return tag 集合
     */
    @Override
    public Set<String> tagSet() {
        return Set.of(ProductTagEnum.CREATED.getTag(), ProductTagEnum.UPDATED.getTag());
    }

    /**
     * 判断事件是否由本处理器处理。
     *
     * @param tag 消息 tag
     * @return true 表示创建或更新事件
     */
    @Override
    public boolean canHandle(String tag) {
        return tagSet().contains(tag);
    }

    /**
     * 提取幂等键。
     *
     * @param event 事件
     * @return 事件 ID
     */
    @Override
    public String eventId(ProductEvent event) {
        return event.getEventId();
    }

    /**
     * 拉取并写入 ES。
     *
     * @param event 事件
     */
    @Override
    public void handle(ProductEvent event) {
        ProductDocResp doc = productClient.searchDoc(event.getProductId());
        if (doc == null) {
            log.warn("商品文档不存在，清理 ES 残留 productId={} eventId={}", event.getProductId(), event.getEventId());
            internalSearchClient.deleteById(event.getProductId());
            return;
        }
        internalSearchClient.upsert(doc);
        log.debug("商品索引写入完成 productId={}", event.getProductId());
    }
}
