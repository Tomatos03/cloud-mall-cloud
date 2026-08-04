package com.cloudmall.order.enums;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 父订单状态迁移表
 * <p>
 * 定义父订单状态的合法迁移集合，与 {@link ParentOrderStatusEnum} 配合使用。
 * 支付前只能整体取消父订单；支付后父订单只做支付聚合，不直接流转履约状态。
 * TIMEOUT、CANCELLED、REFUNDED 为终态。
 *
 * @author : Tomatos
 */
public final class ParentOrderStateTransitions {

    private static final Map<ParentOrderStatusEnum, Set<ParentOrderStatusEnum>> TRANSITIONS = Map.of(
            ParentOrderStatusEnum.CREATING,
            Set.of(ParentOrderStatusEnum.PENDING_PAYMENT, ParentOrderStatusEnum.CANCELLED),
            ParentOrderStatusEnum.PENDING_PAYMENT,
            Set.of(ParentOrderStatusEnum.PAID, ParentOrderStatusEnum.TIMEOUT, ParentOrderStatusEnum.CANCELLED),
            ParentOrderStatusEnum.PAID,
            Set.of(ParentOrderStatusEnum.PARTIAL_REFUNDED, ParentOrderStatusEnum.REFUNDED),
            ParentOrderStatusEnum.PARTIAL_REFUNDED,
            Set.of(ParentOrderStatusEnum.REFUNDED),
            ParentOrderStatusEnum.TIMEOUT,
            Collections.emptySet(),
            ParentOrderStatusEnum.CANCELLED,
            Collections.emptySet(),
            ParentOrderStatusEnum.REFUNDED,
            Collections.emptySet()
    );

    private ParentOrderStateTransitions() {}

    /**
     * 判断父订单状态迁移是否合法。
     *
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return true 为合法迁移
     */
    public static boolean canTransition(ParentOrderStatusEnum from, ParentOrderStatusEnum to) {
        return TRANSITIONS.getOrDefault(from, Collections.emptySet()).contains(to);
    }
}
