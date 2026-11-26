package com.cloudmall.order.application.checkout.step.saga;

import com.cloudmall.order.model.bo.CheckoutContext;

/**
 * Saga 子步骤契约
 * <p>
 * 每个子步骤只负责一个动作；需要回滚的动作覆写 {@link #compensate}，由
 * {@link AbstractSagaSteps} 在异常时逆序调用。补偿必须幂等且尽力而为，
 * 失败由超时恢复任务兜底。不同 Saga 通过各自的标记接口隔离 Bean 注入范围。
 *
 * @author : Tomatos
 */
public interface ISagaSteps {

    /**
     * 子步骤标识（用于排序与日志），由各 Saga 的子步骤枚举实现。
     *
     * @return 子步骤枚举
     */
    Enum<?> name();

    /**
     * 执行子步骤（只做本步骤动作与上下文填充）。
     *
     * @param context 订单上下文
     */
    void execute(CheckoutContext context);

    /**
     * 补偿子步骤。默认空实现，仅需要回滚的步骤覆写。
     *
     * @param context 订单上下文
     */
    default void compensate(CheckoutContext context) {}
}
