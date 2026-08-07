package com.cloudmall.consumer.handler;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.product.api.event.ProductEvent;
import com.cloudmall.product.api.event.ProductTagEnum;
import com.cloudmall.rocketmq.handler.EventHandler;
import com.cloudmall.search.api.client.InternalSearchClient;

/**
 * 商品删除处理器：经 search-service 从 ES 删除对应文档（幂等，文档不存在时忽略）。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDeleteHandler implements EventHandler<ProductEvent> {

    private final InternalSearchClient internalSearchClient;

    /**
     * 本处理器服务的 RocketMQ tag 集合。
     *
     * @return tag 集合
     */
    @Override
    public Set<String> tagSet() {
        return Set.of(ProductTagEnum.DELETED.getTag());
    }

    /**
     * 判断事件是否由本处理器处理。
     *
     * @param tag 消息 tag
     * @return true 表示删除事件
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
     * 删除 ES 文档。
     *
     * @param event 事件
     */
    @Override
    public void handle(ProductEvent event) {
        internalSearchClient.deleteById(event.getProductId());
        log.debug("商品索引删除完成 productId={}", event.getProductId());
    }
}
