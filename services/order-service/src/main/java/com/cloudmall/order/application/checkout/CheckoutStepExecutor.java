package com.cloudmall.order.application.checkout;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.model.req.CheckoutReq;
import com.cloudmall.order.application.checkout.step.CheckoutStepEnum;
import com.cloudmall.order.application.checkout.step.ICheckoutStep;
import com.cloudmall.order.model.bo.CheckoutContext;

import static com.cloudmall.order.application.checkout.step.CheckoutStepEnum.CHECK_SINGLE_ITEM;
import static com.cloudmall.order.application.checkout.step.CheckoutStepEnum.CREATE_SECKILL_ORDER;
import static com.cloudmall.order.application.checkout.step.CheckoutStepEnum.EXECUTE_CHECKOUT_SAGA;
import static com.cloudmall.order.application.checkout.step.CheckoutStepEnum.LOAD_DELIVERY_ADDRESS;
import static com.cloudmall.order.application.checkout.step.CheckoutStepEnum.LOAD_REQUEST_ITEMS;
import static com.cloudmall.order.application.checkout.step.CheckoutStepEnum.LOAD_SELECTED_CART_ITEMS;
import static com.cloudmall.order.application.checkout.step.CheckoutStepEnum.LOAD_TRUSTED_SKU_SNAPSHOTS;
import static com.cloudmall.order.application.checkout.step.CheckoutStepEnum.REMOVE_ORDERED_CART_ITEMS;

/**
 * 结算步骤执行器
 * <p>
 * 持有全部下单步骤，按结算类型映射的步骤列表顺序执行，
 * 步骤缺失或最终未落库订单时抛出 {@link BizErrorEnum#INVALID_ORDER_TYPE}。
 *
 * @author : Tomatos
 */
@Service
public class CheckoutStepExecutor {

    /** 结算类型 → 步骤列表（key 为结算类型，value 为步骤枚举列表，位置即执行顺序） */
    private static final Map<CheckoutTypeEnum, List<CheckoutStepEnum>> STEPS_BY_CHECKOUT_TYPE = new EnumMap<>(
            CheckoutTypeEnum.class
    );

    static {
        STEPS_BY_CHECKOUT_TYPE.put(
                CheckoutTypeEnum.CART,
                List.of(
                        LOAD_DELIVERY_ADDRESS,
                        LOAD_SELECTED_CART_ITEMS,
                        LOAD_TRUSTED_SKU_SNAPSHOTS,
                        EXECUTE_CHECKOUT_SAGA,
                        REMOVE_ORDERED_CART_ITEMS
                )
        );
        STEPS_BY_CHECKOUT_TYPE.put(
                CheckoutTypeEnum.NORMAL,
                List.of(
                        CHECK_SINGLE_ITEM,
                        LOAD_DELIVERY_ADDRESS,
                        LOAD_REQUEST_ITEMS,
                        LOAD_TRUSTED_SKU_SNAPSHOTS,
                        EXECUTE_CHECKOUT_SAGA
                )
        );
        STEPS_BY_CHECKOUT_TYPE.put(
                CheckoutTypeEnum.SECKILL,
                List.of(
                        CHECK_SINGLE_ITEM,
                        LOAD_DELIVERY_ADDRESS,
                        LOAD_REQUEST_ITEMS,
                        LOAD_TRUSTED_SKU_SNAPSHOTS,
                        CREATE_SECKILL_ORDER
                )
        );
    }

    /** 步骤枚举 → 步骤实现（key 为步骤枚举，value 为对应步骤 Bean） */
    private final Map<CheckoutStepEnum, ICheckoutStep> stepByName;

    /**
     * 构造结算步骤执行器。
     *
     * @param steps 全部下单步骤
     */
    public CheckoutStepExecutor(List<ICheckoutStep> steps) {
        Map<CheckoutStepEnum, ICheckoutStep> stepMap = new EnumMap<>(CheckoutStepEnum.class);
        for (ICheckoutStep step : steps) {
            stepMap.put(step.name(), step);
        }
        this.stepByName = stepMap;
    }

    /**
     * 执行指定结算类型的完整下单流程。
     *
     * @param checkoutType 结算类型
     * @param userId 当前登录用户 ID（秒杀内部调用可为 null，由秒杀快照步骤写入）
     * @param req 建单请求
     * @return 订单编号
     */
    public String execute(CheckoutTypeEnum checkoutType, Long userId, CheckoutReq req) {
        CheckoutContext context = buildBaseCheckoutContext(checkoutType, userId, req);
        List<CheckoutStepEnum> stepList = STEPS_BY_CHECKOUT_TYPE.getOrDefault(checkoutType, List.of());
        for (CheckoutStepEnum stepName : stepList) {
            ICheckoutStep step = Asserts.requireNonNull(
                    stepByName.get(stepName),
                    BizErrorEnum.INVALID_ORDER_TYPE
            );
            step.execute(context);
        }
        return Asserts.requireNonNull(context.getOrder(), BizErrorEnum.INVALID_ORDER_TYPE).getOrderNo();
    }

    private static CheckoutContext buildBaseCheckoutContext(
        CheckoutTypeEnum checkoutType,
        Long userId,
        CheckoutReq req
    ) {
        return CheckoutContext.builder()
                              .checkoutType(checkoutType)
                              .userId(userId)
                              .req(req)
                              .build();
    }
}
