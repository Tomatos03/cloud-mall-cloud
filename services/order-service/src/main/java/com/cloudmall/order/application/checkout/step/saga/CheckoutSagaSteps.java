package com.cloudmall.order.application.checkout.step.saga;

import java.util.EnumSet;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.order.application.checkout.step.CheckoutStepEnum;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.model.entity.OrderDO;

/**
 * 结算 Saga 容器步骤
 * <p>
 * 对外仍是结算管道中的 {@code EXECUTE_CHECKOUT_SAGA} 一步；只执行下单建单的四个子步骤，
 * 主干流程（顺序执行、逆序补偿）由 {@link AbstractSagaSteps} 统一提供。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
public class CheckoutSagaSteps extends AbstractSagaSteps {

    /** 建单子步骤：创建骨架 → 预占库存 → 锁券 → 落库明细（不含清车） */
    private static final EnumSet<CheckoutSagaStepsEnum> ORDER = EnumSet.of(
            CheckoutSagaStepsEnum.CREATE_ORDER_SKELETON,
            CheckoutSagaStepsEnum.RESERVE_ORDER_STOCK,
            CheckoutSagaStepsEnum.LOCK_USER_COUPONS,
            CheckoutSagaStepsEnum.WRITE_ORDER_DETAILS
    );

    /**
     * 构造结算 Saga 容器步骤。
     *
     * @param steps 全部 Saga 子步骤 Bean
     */
    public CheckoutSagaSteps(List<ISagaSteps> steps) {
        super(orderByEnum(ORDER, steps));
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
}
