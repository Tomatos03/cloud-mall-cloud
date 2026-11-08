package com.cloudmall.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.enums.SubOrderStatusEnum;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.mapper.OrderSubMapper;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.order.model.entity.OrderSubDO;

/**
 * 用户侧订单操作服务：确认收货、取消未支付订单。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class UserOrderActionService {

    private final OrderMapper orderMapper;
    private final OrderSubMapper orderSubMapper;
    private final IOrderEventService orderEventService;
    private final IOrderStateMachineService orderStateMachineService;

    /**
     * 确认收货：将子订单从已发货推进到已收货、已完成。
     *
     * @param userId 当前用户 ID
     * @param orderNo 子订单号
     */
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long userId, String orderNo) {
        OrderSubDO subOrder = Asserts.requireNonNull(
                orderSubMapper.selectOne(
                        Wrappers.<OrderSubDO>lambdaQuery().eq(OrderSubDO::getSubOrderNo, orderNo)
                ),
                BizErrorEnum.ORDER_NOT_EXIST
        );
        OrderDO parent = Asserts.requireNonNull(
                orderMapper.selectById(subOrder.getParentOrderId()),
                BizErrorEnum.ORDER_NOT_EXIST
        );
        Asserts.isTrue(userId.equals(parent.getUserId()), BizErrorEnum.ORDER_NOT_BELONG_TO_USER);
        Asserts.isTrue(subOrder.getStatus() == SubOrderStatusEnum.SHIPPED, BizErrorEnum.INVALID_ORDER_STATUS);

        orderStateMachineService.transitionSub(
                subOrder.getId(),
                SubOrderStatusEnum.SHIPPED,
                SubOrderStatusEnum.DELIVERED
        );
        orderStateMachineService.transitionSub(
                subOrder.getId(),
                SubOrderStatusEnum.DELIVERED,
                SubOrderStatusEnum.COMPLETED
        );
    }

    /**
     * 取消未支付订单，复用内部取消事件释放库存与优惠券。
     *
     * @param userId 当前用户 ID
     * @param orderNo 父订单号
     */
    public void cancel(Long userId, String orderNo) {
        OrderDO order = Asserts.requireNonNull(
                orderMapper.selectByOrderNo(orderNo),
                BizErrorEnum.ORDER_NOT_EXIST
        );
        Asserts.isTrue(userId.equals(order.getUserId()), BizErrorEnum.ORDER_NOT_BELONG_TO_USER);
        Asserts.isTrue(
                order.getStatus() == ParentOrderStatusEnum.CREATING
                        || order.getStatus() == ParentOrderStatusEnum.PENDING_PAYMENT,
                BizErrorEnum.ORDER_STATUS_NOT_CREATED
        );
        orderEventService.handleCancel(orderNo);
    }
}
