package com.cloudmall.order.application.checkout.step;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.cart.api.client.InternalCartClient;
import com.cloudmall.cart.api.model.resp.CartSelectedItemResp;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.bo.CheckoutContext;

/**
 * 加载购物车已选中商品步骤：通过 Feign 读取购物车已选中项，组装为 skuId → 下单数量写入 context。
 * <p>
 * 购物车内部接口直读数据库、绕过缓存，避免陈旧的选中状态影响下单。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class LoadSelectedCartItemsStep implements ICheckoutStep {

    private final InternalCartClient internalCartClient;

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
        List<CartSelectedItemResp> selected = internalCartClient.listSelectedItems(context.getUserId());
        Asserts.notEmpty(selected, BizErrorEnum.PRODUCT_NOT_IN_CART);
        Map<Long, Integer> checkoutItems = new LinkedHashMap<>();
        for (CartSelectedItemResp item : selected) {
            checkoutItems.put(item.getSkuId(), item.getQuantity());
        }
        context.setCheckoutItems(checkoutItems);
    }
}
