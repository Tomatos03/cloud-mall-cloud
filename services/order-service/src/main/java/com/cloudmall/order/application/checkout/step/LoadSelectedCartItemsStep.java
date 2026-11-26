package com.cloudmall.order.application.checkout.step;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.model.entity.CartDO;
import com.cloudmall.order.service.ICartService;

/**
 * 加载购物车已选中商品步骤：加载用户全部选中购物车项，组装为 skuId → 下单数量写入 context。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class LoadSelectedCartItemsStep implements ICheckoutStep {

    private final ICartService cartService;

    /**
     * 步骤名称。
     *
     * @return 加载购物车已选中商品
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.LOAD_SELECTED_CART_ITEMS;
    }

    /**
     * 执行步骤。
     *
     * @param context 订单上下文
     */
    @Override
    public void execute(CheckoutContext context) {
        List<CartDO> selected = cartService.listSelectedItems(context.getUserId());
        Asserts.notEmpty(selected, BizErrorEnum.PRODUCT_NOT_IN_CART);
        Map<Long, Integer> checkoutItems = new LinkedHashMap<>();
        for (CartDO cart : selected) {
            checkoutItems.put(cart.getSkuId(), cart.getQuantity());
        }
        context.setCheckoutItems(checkoutItems);
    }
}
