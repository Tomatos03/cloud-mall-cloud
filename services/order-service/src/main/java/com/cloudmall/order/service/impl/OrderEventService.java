package com.cloudmall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.service.IOrderEventService;
import com.cloudmall.order.service.IOrderOutboxService;
import com.cloudmall.order.service.IOrderStateMachineService;

/**
 * 订单事件服务实现
 * <p>
 * 支付/超时/取消/退款事件（本阶段为模拟入口）：
 * 校验状态迁移并更新父订单，同时写入事件 Outbox（同事务）。
 * 事件由 Outbox 发布任务异步发送，coupon-service 消费后核销/释放/回退用户券。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderEventService implements IOrderEventService {

    private final OrderMapper orderMapper;
    private final IOrderStateMachineService stateMachine;
    private final IOrderOutboxService orderOutboxService;

    /**
     * 支付成功：PENDING_PAYMENT → PAID。
     *
     * @param orderNo 父订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaid(String orderNo) {
        OrderDO order = requireOrder(orderNo);
        stateMachine.transitionParent(
                order.getId(),
                ParentOrderStatusEnum.PENDING_PAYMENT,
                ParentOrderStatusEnum.PAID
        );
        orderOutboxService.appendOrderEvent(orderNo, order.getUserId(), EVENT_PAID, null);
    }

    /**
     * 支付超时：PENDING_PAYMENT → TIMEOUT。
     *
     * @param orderNo 父订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeout(String orderNo) {
        OrderDO order = requireOrder(orderNo);
        stateMachine.transitionParent(
                order.getId(),
                ParentOrderStatusEnum.PENDING_PAYMENT,
                ParentOrderStatusEnum.TIMEOUT
        );
        orderOutboxService.appendOrderEvent(orderNo, order.getUserId(), EVENT_TIMEOUT, null);
    }

    /**
     * 订单取消：待支付父订单整体取消（CREATING 或 PENDING_PAYMENT）。
     *
     * @param orderNo 父订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCancel(String orderNo) {
        OrderDO order = requireOrder(orderNo);
        int updated = orderMapper.update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, ParentOrderStatusEnum.CANCELLED)
                        .eq(OrderDO::getId, order.getId())
                        .in(
                                OrderDO::getStatus,
                                ParentOrderStatusEnum.CREATING,
                                ParentOrderStatusEnum.PENDING_PAYMENT
                        )
        );
        AssertUtils.isTrue(updated > 0, BizErrorCode.INVALID_ORDER_STATUS);
        orderOutboxService.appendOrderEvent(orderNo, order.getUserId(), EVENT_CANCELLED, null);
    }

    /**
     * 全额退款：PAID/PARTIAL_REFUNDED → REFUNDED。
     *
     * @param orderNo 父订单号
     * @param refundId 退款单 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFullyRefunded(String orderNo, String refundId) {
        AssertUtils.assertNotBlank(refundId, BizErrorCode.INVALID_PARAM);
        OrderDO order = requireOrder(orderNo);
        int updated = orderMapper.update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, ParentOrderStatusEnum.REFUNDED)
                        .eq(OrderDO::getId, order.getId())
                        .in(
                                OrderDO::getStatus,
                                ParentOrderStatusEnum.PAID,
                                ParentOrderStatusEnum.PARTIAL_REFUNDED
                        )
        );
        AssertUtils.isTrue(updated > 0, BizErrorCode.INVALID_ORDER_STATUS);
        orderOutboxService.appendOrderEvent(
                orderNo,
                order.getUserId(),
                EVENT_FULLY_REFUNDED,
                refundId
        );
    }

    /**
     * 按订单号查询父订单。
     *
     * @param orderNo 父订单号
     * @return 父订单
     */
    private OrderDO requireOrder(String orderNo) {
        OrderDO order = orderMapper.selectOne(
                Wrappers.<OrderDO>lambdaQuery().eq(OrderDO::getOrderNo, orderNo)
        );
        AssertUtils.notNull(order, BizErrorCode.ORDER_NOT_EXIST);
        return order;
    }
}
