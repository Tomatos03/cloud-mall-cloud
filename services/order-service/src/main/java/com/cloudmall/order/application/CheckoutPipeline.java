package com.cloudmall.order.application;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.request.CreateOrderReq;
import com.cloudmall.order.model.CheckoutContext;
import com.cloudmall.order.step.CheckoutStep;
import com.cloudmall.order.step.CheckoutStepEnum;

import static com.cloudmall.order.step.CheckoutStepEnum.CLEAN_CART;
import static com.cloudmall.order.step.CheckoutStepEnum.LOAD_ADDRESS;
import static com.cloudmall.order.step.CheckoutStepEnum.LOAD_CART_CHECKOUT_ITEMS;
import static com.cloudmall.order.step.CheckoutStepEnum.LOAD_REQUEST_CHECKOUT_ITEMS;
import static com.cloudmall.order.step.CheckoutStepEnum.LOAD_SKUS;
import static com.cloudmall.order.step.CheckoutStepEnum.PLACE_PRE_ORDER;
import static com.cloudmall.order.step.CheckoutStepEnum.PLACE_SECKILL_ORDER;
import static com.cloudmall.order.step.CheckoutStepEnum.VALIDATE_SINGLE_ITEM;

/**
 * 结算管道
 * <p>
 * 持有全部下单步骤，按结算类型映射的步骤列表顺序执行，
 * 步骤缺失或最终未落库订单时抛出 {@link BizError#INVALID_ORDER_TYPE}。
 *
 * @author : Tomatos
 */
@Service
public class CheckoutPipeline {

    /** 结算类型 → 步骤列表（key 为结算类型，value 为步骤枚举列表，位置即执行顺序） */
    private static final Map<CheckoutTypeEnum, List<CheckoutStepEnum>> STEPS_BY_CHECKOUT_TYPE = buildSteps();

    /**
     * 构建结算类型 → 步骤列表的枚举映射。
     *
     * @return 结算类型步骤编排
     */
    private static Map<CheckoutTypeEnum, List<CheckoutStepEnum>> buildSteps() {
        Map<CheckoutTypeEnum, List<CheckoutStepEnum>> steps = new EnumMap<>(CheckoutTypeEnum.class);
        steps.put(
                CheckoutTypeEnum.CART,
                List.of(LOAD_ADDRESS, LOAD_CART_CHECKOUT_ITEMS, LOAD_SKUS, PLACE_PRE_ORDER, CLEAN_CART)
        );
        steps.put(
                CheckoutTypeEnum.DIRECT,
                List.of(
                        VALIDATE_SINGLE_ITEM,
                        LOAD_ADDRESS,
                        LOAD_REQUEST_CHECKOUT_ITEMS,
                        LOAD_SKUS,
                        PLACE_PRE_ORDER
                )
        );
        steps.put(
                CheckoutTypeEnum.SECKILL,
                List.of(
                        VALIDATE_SINGLE_ITEM,
                        LOAD_ADDRESS,
                        LOAD_REQUEST_CHECKOUT_ITEMS,
                        LOAD_SKUS,
                        PLACE_SECKILL_ORDER
                )
        );
        return steps;
    }

    /** 步骤枚举 → 步骤实现（key 为步骤枚举，value 为对应步骤 Bean） */
    private final Map<CheckoutStepEnum, CheckoutStep> stepByName;

    /**
     * 构造结算管道。
     *
     * @param steps 全部下单步骤
     */
    public CheckoutPipeline(List<CheckoutStep> steps) {
        Map<CheckoutStepEnum, CheckoutStep> stepMap = new EnumMap<>(CheckoutStepEnum.class);
        for (CheckoutStep step : steps) {
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
    public String execute(CheckoutTypeEnum checkoutType, Long userId, CreateOrderReq req) {
        CheckoutContext context = buildBaseCheckoutContext(checkoutType, userId, req);
        List<CheckoutStepEnum> stepList = STEPS_BY_CHECKOUT_TYPE.getOrDefault(checkoutType, List.of());
        for (CheckoutStepEnum stepName : stepList) {
            CheckoutStep step = Asserts.requireNonNull(stepByName.get(stepName), BizError.INVALID_ORDER_TYPE);
            step.process(context);
        }
        return Asserts.requireNonNull(context.getOrder(), BizError.INVALID_ORDER_TYPE).getOrderNo();
    }

    private static CheckoutContext buildBaseCheckoutContext(
        CheckoutTypeEnum checkoutType,
        Long userId,
        CreateOrderReq req
    ) {
        return CheckoutContext.builder()
                              .checkoutType(checkoutType)
                              .userId(userId)
                              .req(req)
                              .build();
    }
}
