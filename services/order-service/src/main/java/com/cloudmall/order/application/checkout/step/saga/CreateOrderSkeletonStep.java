package com.cloudmall.order.application.checkout.step.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.order.service.impl.OrderService;

/**
 * 创建订单骨架子步骤
 * <p>
 * 骨架先于远程调用提交，宕机时才能被超时恢复任务发现；失败补偿为标记订单取消。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderSkeletonStep implements ICheckoutSagaSteps {

    private final OrderService orderService;

    @Override
    public CheckoutSagaStepsEnum name() {
        return CheckoutSagaStepsEnum.CREATE_ORDER_SKELETON;
    }

    @Override
    public void execute(CheckoutContext context) {
        OrderDO order = orderService.createPreOrder(context);
        context.setOrder(order);
    }

    @Override
    public void compensate(CheckoutContext context) {
        OrderDO order = context.getOrder();
        if (order == null) {
            return;
        }
        try {
            orderService.markCancelled(order.getId());
        } catch (RuntimeException e) {
            log.warn("Saga 补偿标记订单取消失败 orderNo={}", order.getOrderNo(), e);
        }
    }
}
