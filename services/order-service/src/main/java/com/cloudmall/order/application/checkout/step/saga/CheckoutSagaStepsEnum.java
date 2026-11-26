package com.cloudmall.order.application.checkout.step.saga;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结算 Saga 子步骤枚举
 * <p>
 * 用于子步骤注册与执行顺序标识，位置即执行顺序；异常时按逆序执行补偿。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CheckoutSagaStepsEnum {
    /** 创建 CREATING 订单骨架 */
    CREATE_ORDER_SKELETON("创建订单骨架"),

    /** 按结算项预占商品库存 */
    RESERVE_ORDER_STOCK("预占订单库存"),

    /** 锁定用户券产出优惠快照 */
    LOCK_USER_COUPONS("锁定用户优惠券"),

    /** 落库金额/明细/优惠并置待支付 */
    WRITE_ORDER_DETAILS("落库订单金额与明细");
    /** 步骤描述 */
    private final String desc;
}
