package com.cloudmall.order.application.checkout.step.saga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.ListUtil;
import lombok.extern.slf4j.Slf4j;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.application.checkout.step.ICheckoutStep;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.model.entity.OrderDO;

/**
 * Saga 容器步骤抽象基类
 * <p>
 * 持有 Saga 主干流程：顺序执行子步骤，只在成功后登记；任一环节失败则逆序补偿已成功步骤，
 * 补偿尽力而为，单个补偿失败不影响其余补偿，最后上抛原异常。子类只负责提供有序子步骤，
 * 不得覆写 {@link #execute}，从而保证各 Saga 容器的补偿语义一致。
 *
 * @author : Tomatos
 */
@Slf4j
public abstract class AbstractSagaSteps implements ICheckoutStep {

    private final List<ISagaSteps> steps;

    /**
     * 构造 Saga 容器步骤。
     *
     * @param steps 已按执行顺序排列的子步骤
     */
    protected AbstractSagaSteps(List<ISagaSteps> steps) {
        this.steps = List.copyOf(steps);
    }

    /**
     * 按给定枚举顺序排列子步骤，缺失步骤视为配置错误。
     *
     * @param order 本容器需要的子步骤及执行顺序
     * @param steps 容器中注入的全部子步骤 Bean
     * @return 有序子步骤列表
     */
    protected static List<ISagaSteps> orderByEnum(
        Collection<CheckoutSagaStepsEnum> order,
        List<ISagaSteps> steps
    ) {
        Map<CheckoutSagaStepsEnum, ISagaSteps> byEnum = new EnumMap<>(CheckoutSagaStepsEnum.class);
        for (ISagaSteps step : steps) {
            byEnum.put(step.name(), step);
        }
        return order.stream()
                    .map(byEnum::get)
                    .map(step -> Asserts.requireNonNull(step, BizErrorEnum.INVALID_ORDER_TYPE))
                    .toList();
    }

    @Override
    public final void execute(CheckoutContext context) {
        List<ISagaSteps> executed = new ArrayList<>();
        try {
            for (ISagaSteps step : steps) {
                step.execute(context);
                executed.add(step);
            }
        } catch (RuntimeException e) {
            compensate(executed, context);
            throw e;
        }
        afterExecute(context);
    }

    /**
     * 全部子步骤成功后的扩展点，默认空实现。
     *
     * @param context 订单上下文
     */
    protected void afterExecute(CheckoutContext context) {}

    /**
     * 逆序补偿已成功的子步骤，单个补偿失败不影响其余补偿。
     *
     * @param executed 已成功执行的子步骤
     * @param context 订单上下文
     */
    private void compensate(List<ISagaSteps> executed, CheckoutContext context) {
        for (ISagaSteps step : ListUtil.reverseNew(executed)) {
            try {
                step.compensate(context);
            } catch (RuntimeException compensationFailure) {
                OrderDO order = context.getOrder();
                log.warn(
                        "Saga 补偿失败 step={} orderNo={}",
                        step.name(),
                        order == null ? null : order.getOrderNo(),
                        compensationFailure
                );
            }
        }
    }
}
