package com.cloudmall.order.step;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.model.bo.OrderItem;
import com.cloudmall.order.model.bo.CheckoutContext;

/**
 * 请求结算项加载步骤：校验下单明细 SKU 不重复且数量合法，组装为 skuId → 下单数量写入 context。
 *
 * @author : Tomatos
 */
@Component
public class LoadRequestCheckoutItemsStep implements ICheckoutStep {

    /**
     * 步骤名称。
     *
     * @return 请求结算项加载
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.LOAD_REQUEST_CHECKOUT_ITEMS;
    }

    /**
     * 执行步骤。
     *
     * @param ctx 订单上下文
     */
    @Override
    public void process(CheckoutContext ctx) {
        Map<Long, Integer> checkoutItems = new LinkedHashMap<>();
        for (OrderItem item : ctx.getReq().getItems()) {
            Asserts.isFalse(checkoutItems.containsKey(item.getSkuId()), BizErrorEnum.INVALID_PARAM);
            Asserts.greaterThan(item.getQuantity(), 0, BizErrorEnum.INVALID_PARAM);
            checkoutItems.put(item.getSkuId(), item.getQuantity());
        }
        ctx.setCheckoutItems(checkoutItems);
    }
}
