package com.cloudmall.order.service;

import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.enums.SubOrderStatusEnum;

/**
 * 订单状态机服务
 * <p>
 * 统一校验并执行父订单、子订单状态迁移，非法迁移抛业务异常。
 *
 * @author : Tomatos
 */
public interface IOrderStateMachineService {

    /**
     * 校验并迁移父订单状态，返回更新影响行数（0 表示状态已变化，防并发覆盖）。
     *
     * @param orderId 父订单 ID
     * @param from 迁移前状态
     * @param to 迁移后状态
     */
    void transitionParent(Long orderId, ParentOrderStatusEnum from, ParentOrderStatusEnum to);

    /**
     * 校验并迁移子订单状态。
     *
     * @param subOrderId 子订单 ID
     * @param from 迁移前状态
     * @param to 迁移后状态
     */
    void transitionSub(Long subOrderId, SubOrderStatusEnum from, SubOrderStatusEnum to);

    /**
     * 判断父订单状态迁移是否合法。
     *
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return true 为合法迁移
     */
    boolean canParentTransition(ParentOrderStatusEnum from, ParentOrderStatusEnum to);

    /**
     * 判断子订单状态迁移是否合法。
     *
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return true 为合法迁移
     */
    boolean canSubTransition(SubOrderStatusEnum from, SubOrderStatusEnum to);
}
