package com.cloudmall.order.application.checkout.step.saga;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.order.service.impl.OrderService;

/**
 * 落库订单金额与明细子步骤
 * <p>
 * 金额/明细/优惠落库并置为待支付，整体在本地事务内完成，失败即回滚，无需额外补偿。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class WriteOrderDetailsStep implements ICheckoutSagaSteps {

    private final OrderService orderService;

    @Override
    public CheckoutSagaStepsEnum name() {
        return CheckoutSagaStepsEnum.WRITE_ORDER_DETAILS;
    }

    @Override
    public void execute(CheckoutContext context) {
        OrderDO order = context.getOrder();
        orderService.finalizeOrder(order, context, context.getCouponCalc());
    }
}
