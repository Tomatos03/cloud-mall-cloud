package com.cloudmall.consumer.event;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.springframework.stereotype.Component;

import com.cloudmall.product.api.event.ProductEvent;
import com.cloudmall.rocketmq.consumer.AbstractMessageConsumer;
import com.cloudmall.rocketmq.consumer.IMessageConsumeService;
import com.cloudmall.rocketmq.handler.EventHandler;

/**
 * 商品事件消费者
 * <p>
 * 消费 product-service 发布的商品生命周期事件（PRODUCT_EVENTS），
 * 按 tag 分发到写入/删除处理器，经 search-service 完成 ES 商品索引增量更新。
 *
 * @author : Tomatos
 */
@Component
@RocketMQMessageListener(
        topic = "PRODUCT_EVENTS",
        consumerGroup = "search-product-events",
        selectorType = SelectorType.TAG,
        selectorExpression = "ProductCreated || ProductUpdated || ProductDeleted"
)
public class ProductEventConsumer extends AbstractMessageConsumer<ProductEvent> {

    /**
     * 构造器。
     *
     * @param consumeService 消费幂等服务
     * @param handlers 商品事件处理器
     */
    public ProductEventConsumer(
        IMessageConsumeService consumeService,
        List<EventHandler<ProductEvent>> handlers
    ) {
        super(consumeService, handlers, ProductEvent.class);
    }
}
