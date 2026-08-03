package com.cloudmall.order.enums;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 店铺子订单状态迁移表
 * <p>
 * 定义子订单状态的合法迁移集合，与 {@link SubOrderStatusEnum} 配合使用。
 * 支付前父订单整体取消时子订单进入 CANCELLED；支付后子订单可独立
 * 发货、收货、完成和全额退款。CANCELLED、REFUNDED 为终态。
 *
 * @author : Tomatos
 */
public final class SubOrderStateTransitions {

    private static final Map<SubOrderStatusEnum, Set<SubOrderStatusEnum>> TRANSITIONS = Map.of(
            SubOrderStatusEnum.PENDING_SHIPMENT,
            Set.of(
                    SubOrderStatusEnum.SHIPPED,
                    SubOrderStatusEnum.CANCELLED,
                    SubOrderStatusEnum.REFUNDED
            ),
            SubOrderStatusEnum.SHIPPED,
            Set.of(SubOrderStatusEnum.DELIVERED, SubOrderStatusEnum.REFUNDED),
            SubOrderStatusEnum.DELIVERED,
            Set.of(SubOrderStatusEnum.COMPLETED, SubOrderStatusEnum.REFUNDED),
            SubOrderStatusEnum.COMPLETED,
            Set.of(SubOrderStatusEnum.REFUNDED),
            SubOrderStatusEnum.CANCELLED,
            Collections.emptySet(),
            SubOrderStatusEnum.REFUNDED,
            Collections.emptySet()
    );

    private SubOrderStateTransitions() {}

    /**
     * 判断子订单状态迁移是否合法。
     *
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return true 为合法迁移
     */
    public static boolean canTransition(SubOrderStatusEnum from, SubOrderStatusEnum to) {
        return TRANSITIONS.getOrDefault(from, Collections.emptySet()).contains(to);
    }
}
