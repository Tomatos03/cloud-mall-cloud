package com.cloudmall.order.step;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.entity.CartDO;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.service.ICartService;

/**
 * 购物车结算项加载步骤：加载用户全部选中购物车项，组装为 skuId → 下单数量写入 context。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class LoadCartCheckoutItemsStep implements ICheckoutStep {

    private final ICartService cartService;

    /**
     * 步骤名称。
     *
     * @return 购物车结算项加载
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.LOAD_CART_CHECKOUT_ITEMS;
    }

    /**
     * 执行步骤。
     *
     * @param ctx 订单上下文
     */
    @Override
    public void process(CheckoutContext ctx) {
        List<CartDO> selected = cartService.listSelectedItems(ctx.getUserId());
        Asserts.notEmpty(selected, BizErrorEnum.PRODUCT_NOT_IN_CART);
        Map<Long, Integer> checkoutItems = new LinkedHashMap<>();
        for (CartDO cart : selected) {
            checkoutItems.put(cart.getSkuId(), cart.getQuantity());
        }
        ctx.setCheckoutItems(checkoutItems);
    }
}
