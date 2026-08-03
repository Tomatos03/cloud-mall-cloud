package com.cloudmall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderSubDO;
import com.cloudmall.order.enums.ParentOrderStateTransitions;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.enums.SubOrderStateTransitions;
import com.cloudmall.order.enums.SubOrderStatusEnum;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.mapper.OrderSubMapper;
import com.cloudmall.order.service.IOrderStateMachineService;

/**
 * 订单状态机服务实现
 * <p>
 * 基于状态迁移表校验合法迁移，通过带迁移前状态的条件更新防止并发覆盖。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderStateMachineService implements IOrderStateMachineService {

    private final OrderMapper orderMapper;
    private final OrderSubMapper orderSubMapper;

    /**
     * 校验并迁移父订单状态，非法迁移抛异常。
     *
     * @param orderId 父订单 ID
     * @param from 迁移前状态
     * @param to 迁移后状态
     */
    @Override
    public void transitionParent(Long orderId, ParentOrderStatusEnum from, ParentOrderStatusEnum to) {
        AssertUtils.isTrue(canParentTransition(from, to), BizErrorCode.INVALID_ORDER_STATUS);
        int updated = orderMapper.update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, to)
                        .eq(OrderDO::getId, orderId)
                        .eq(OrderDO::getStatus, from)
        );
        AssertUtils.isTrue(updated > 0, BizErrorCode.INVALID_ORDER_STATUS);
    }

    /**
     * 校验并迁移子订单状态，非法迁移抛异常。
     *
     * @param subOrderId 子订单 ID
     * @param from 迁移前状态
     * @param to 迁移后状态
     */
    @Override
    public void transitionSub(Long subOrderId, SubOrderStatusEnum from, SubOrderStatusEnum to) {
        AssertUtils.isTrue(canSubTransition(from, to), BizErrorCode.INVALID_ORDER_STATUS);
        int updated = orderSubMapper.update(
                Wrappers.<OrderSubDO>lambdaUpdate()
                        .set(OrderSubDO::getStatus, to)
                        .eq(OrderSubDO::getId, subOrderId)
                        .eq(OrderSubDO::getStatus, from)
        );
        AssertUtils.isTrue(updated > 0, BizErrorCode.INVALID_ORDER_STATUS);
    }

    /**
     * 判断父订单状态迁移是否合法。
     *
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return true 为合法迁移
     */
    @Override
    public boolean canParentTransition(ParentOrderStatusEnum from, ParentOrderStatusEnum to) {
        return ParentOrderStateTransitions.canTransition(from, to);
    }

    /**
     * 判断子订单状态迁移是否合法。
     *
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return true 为合法迁移
     */
    @Override
    public boolean canSubTransition(SubOrderStatusEnum from, SubOrderStatusEnum to) {
        return SubOrderStateTransitions.canTransition(from, to);
    }
}
