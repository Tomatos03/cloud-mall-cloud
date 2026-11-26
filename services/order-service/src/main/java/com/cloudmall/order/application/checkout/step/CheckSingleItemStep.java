package com.cloudmall.order.application.checkout.step;

import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.bo.CheckoutContext;

/**
 * 检查是否为单商品项步骤：校验请求 items 仅一条且数量合法。
 *
 * @author : Tomatos
 */
@Component
public class CheckSingleItemStep implements ICheckoutStep {

    /**
     * 步骤名称。
     *
     * @return 检查是否为单商品项
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.CHECK_SINGLE_ITEM;
    }

    /**
     * 执行步骤。
     *
     * @param context 订单上下文
     */
    @Override
    public void execute(CheckoutContext context) {
        Asserts.isTrue(context.getReq()
                              .getItems()
                              .size() == 1, BizErrorEnum.INVALID_PARAM);
    }
}
