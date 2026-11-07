package com.cloudmall.order.step;

import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.bo.CheckoutContext;

/**
 * 单商品校验步骤：items 仅一条且数量合法。
 *
 * @author : Tomatos
 */
@Component
public class ValidateSingleItemStep implements ICheckoutStep {

    /**
     * 步骤名称。
     *
     * @return 单商品校验
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.VALIDATE_SINGLE_ITEM;
    }

    /**
     * 执行步骤。
     *
     * @param ctx 订单上下文
     */
    @Override
    public void process(CheckoutContext ctx) {
        Asserts.isTrue(ctx.getReq()
                          .getItems()
                          .size() == 1, BizErrorEnum.INVALID_PARAM);
    }
}
