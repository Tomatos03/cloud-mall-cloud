package com.cloudmall.order.step;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.service.ICartService;

/**
 * 清理购物车步骤：删除已下单的购物车项。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class CleanCartStep implements ICheckoutStep {

    private final ICartService cartService;

    /**
     * 步骤名称。
     *
     * @return 清理购物车
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.CLEAN_CART;
    }

    /**
     * 执行步骤。
     *
     * @param ctx 订单上下文
     */
    @Override
    public void process(CheckoutContext ctx) {
        List<Long> skuIds = ctx.getCheckoutItems()
                               .keySet()
                               .stream()
                               .toList();
        cartService.removeItems(ctx.getUserId(), skuIds);
    }
}
