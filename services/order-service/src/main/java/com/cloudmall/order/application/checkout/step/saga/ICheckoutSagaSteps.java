package com.cloudmall.order.application.checkout.step.saga;

/**
 * 预订单 Saga 子步骤标记接口
 * <p>
 * 用于隔离 Spring 注入范围：预订单容器只注入本接口的实现，避免混入其它 Saga 的子步骤。
 *
 * @author : Tomatos
 */
public interface ICheckoutSagaSteps extends ISagaSteps {

    @Override
    CheckoutSagaStepsEnum name();
}
