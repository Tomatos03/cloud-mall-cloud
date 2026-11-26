package com.cloudmall.order.application.checkout.step;

import java.util.List;

import cn.hutool.core.collection.CollStreamUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.cart.api.client.InternalCartClient;
import com.cloudmall.order.model.bo.CheckoutContext;

/**
 * 删除已下单的购物车项步骤：结算管道的收尾步骤，通过 Feign 通知购物车服务移除对应记录。
 * <p>
 * 尽力而为：订单已落库，清车失败只记日志、不抛异常，不触发结算 Saga 补偿，
 * 也不恢复购物车；购物车残留属于可接受的降级，由用户或后续任务处理。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveOrderedCartItemsStep implements ICheckoutStep {

    private final InternalCartClient internalCartClient;

    /**
     * 步骤名称。
     *
     * @return 删除已下单的购物车项
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.REMOVE_ORDERED_CART_ITEMS;
    }

    @Override
    public void execute(CheckoutContext context) {
        List<Long> skuIds = CollStreamUtil.toList(context.getCheckoutItems().keySet(), skuId -> skuId);
        try {
            internalCartClient.removeItems(context.getUserId(), skuIds);
        } catch (RuntimeException e) {
            log.warn("清车失败但订单已创建，跳过 orderNo={} skuIds={}", context.getOrder().getOrderNo(), skuIds, e);
        }
    }
}
