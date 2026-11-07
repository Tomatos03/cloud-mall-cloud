package com.cloudmall.order.step;

import com.cloudmall.common.exception.BizException;
import com.cloudmall.order.model.bo.CheckoutContext;

/**
 * 结算步骤
 * <p>
 * 一个步骤负责：加载数据 + 校验 + 填充上下文，由 {@link com.cloudmall.order.application.CheckoutPipeline}
 * 按结算类型映射的列表顺序执行。
 *
 * @author : Tomatos
 */
public interface ICheckoutStep {

    /**
     * 步骤名称（用于日志、监控、排序标识）。
     *
     * @return 步骤枚举
     */
    CheckoutStepEnum name();

    /**
     * 执行步骤（加载数据 + 校验 + 填充上下文）。
     *
     * @param ctx 订单上下文
     * @throws BizException 校验失败时抛出
     */
    void process(CheckoutContext ctx);
}
