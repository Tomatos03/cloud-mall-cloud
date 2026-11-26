package com.cloudmall.order.application.checkout.step;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.service.ICartService;

/**
 * 删除已下单的购物车项步骤：按结算项中的 SKU 从购物车移除对应记录。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class RemoveOrderedCartItemsStep implements ICheckoutStep {

    private final ICartService cartService;

    /**
     * 步骤名称。
     *
     * @return 删除已下单的购物车项
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.REMOVE_ORDERED_CART_ITEMS;
    }

    /**
     * 执行步骤。
     *
     * @param context 订单上下文
     */
    @Override
    public void execute(CheckoutContext context) {
        List<Long> skuIds = context.getCheckoutItems()
                                   .keySet()
                                   .stream()
                                   .toList();
        cartService.removeItems(context.getUserId(), skuIds);
    }
}
