package com.cloudmall.order.application.checkout.step.saga;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.application.checkout.step.CheckoutStepEnum;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.model.entity.OrderDO;

/**
 * 结算 Saga 容器步骤
 * <p>
 * 对外仍是结算管道中的 {@code EXECUTE_CHECKOUT_SAGA} 一步；自身只定义子步骤与顺序，
 * 主干流程（顺序执行、逆序补偿）由 {@link AbstractSagaSteps} 统一提供。
 * 补偿边界仅限本 Saga，不波及结算管道的后续步骤（如清理购物车）。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
public class CheckoutSagaSteps extends AbstractSagaSteps {

    /**
     * 构造结算 Saga 容器步骤。
     *
     * @param steps 结算 Saga 子步骤（仅本 Saga 的实现，由标记接口隔离）
     */
    public CheckoutSagaSteps(List<ICheckoutSagaSteps> steps) {
        super(orderByEnum(steps));
    }

    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.EXECUTE_CHECKOUT_SAGA;
    }

    @Override
    protected void afterExecute(CheckoutContext context) {
        OrderDO order = context.getOrder();
        log.info("下单成功 orderNo={} orderId={}", order.getOrderNo(), order.getId());
    }

    /**
     * 按枚举声明顺序排列子步骤，缺失步骤视为配置错误。
     *
     * @param steps 预订单 Saga 子步骤
     * @return 有序子步骤列表
     */
    private static List<ICheckoutSagaSteps> orderByEnum(List<ICheckoutSagaSteps> steps) {
        Map<CheckoutSagaStepsEnum, ICheckoutSagaSteps> byEnum = new EnumMap<>(CheckoutSagaStepsEnum.class);
        for (ICheckoutSagaSteps step : steps) {
            byEnum.put(step.name(), step);
        }
        return Arrays.stream(CheckoutSagaStepsEnum.values())
                     .map(byEnum::get)
                     .map(step -> Asserts.requireNonNull(step, BizErrorEnum.INVALID_ORDER_TYPE))
                     .toList();
    }
}
