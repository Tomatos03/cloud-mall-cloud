package com.cloudmall.consumer.handler;

import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.order.api.client.OrderClient;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.model.OrderItemReq;
import com.cloudmall.order.api.request.CreateOrderReq;
import com.cloudmall.rocketmq.handler.EventHandler;
import com.cloudmall.seckill.api.event.SeckillOrderCreateEvent;
import com.cloudmall.seckill.api.event.SeckillTagEnum;

/**
 * 秒杀建单处理器
 * <p>
 * 消费抢购事件后调 order-service 统一建单接口建真实订单，
 * 订单号由秒杀侧预生成，order-service 按 uk_order_no 幂等。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderCreateHandler implements EventHandler<SeckillOrderCreateEvent> {

    private final OrderClient orderClient;

    /**
     * 本处理器服务的 RocketMQ tag 集合。
     *
     * @return tag 集合
     */
    @Override
    public Set<String> tagSet() {
        return Set.of(SeckillTagEnum.ORDER_CREATE.getTag());
    }

    /**
     * 判断事件是否由本处理器处理。
     *
     * @param tag 消息 tag
     * @return true 表示秒杀建单事件
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
    public String eventId(SeckillOrderCreateEvent event) {
        return event.getEventId();
    }

    /**
     * 调 order-service 建真实秒杀订单。
     *
     * @param event 事件
     */
    @Override
    public void handle(SeckillOrderCreateEvent event) {
        orderClient.createOrder(
                CheckoutTypeEnum.SECKILL,
                CreateOrderReq
                        .builder()
                        .orderNo(event.getOrderNo())
                        .addressId(event.getAddressId())
                        .items(
                                List.of(
                                        OrderItemReq.builder()
                                                    .skuId(event.getSkuId())
                                                    .quantity(event.getQuantity())
                                                    .build()
                                )
                        )
                        .build()
        );
        log.info("秒杀真实订单已创建 orderNo={}", event.getOrderNo());
    }
}
