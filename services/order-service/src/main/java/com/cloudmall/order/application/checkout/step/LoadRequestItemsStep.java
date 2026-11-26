package com.cloudmall.order.application.checkout.step;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.model.dto.OrderItem;
import com.cloudmall.order.model.bo.CheckoutContext;

/**
 * 加载请求中的商品项步骤：校验下单明细 SKU 不重复且数量合法，组装为 skuId → 下单数量写入 context。
 *
 * @author : Tomatos
 */
@Component
public class LoadRequestItemsStep implements ICheckoutStep {

    /**
     * 步骤名称。
     *
     * @return 加载请求中的商品项
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.LOAD_REQUEST_ITEMS;
    }

    /**
     * 执行步骤。
     *
     * @param context 订单上下文
     */
    @Override
    public void execute(CheckoutContext context) {
        Map<Long, Integer> checkoutItems = new LinkedHashMap<>();
        for (OrderItem item : context.getReq().getItems()) {
            Asserts.isFalse(checkoutItems.containsKey(item.getSkuId()), BizErrorEnum.INVALID_PARAM);
            Asserts.greaterThan(item.getQuantity(), 0, BizErrorEnum.INVALID_PARAM);
            checkoutItems.put(item.getSkuId(), item.getQuantity());
        }
        context.setCheckoutItems(checkoutItems);
    }
}
