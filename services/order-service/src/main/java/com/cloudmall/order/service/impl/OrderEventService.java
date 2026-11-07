package com.cloudmall.order.service.impl;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.model.bo.OrderEvent;
import com.cloudmall.order.api.enums.OrderTagEnum;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.service.IOrderEventService;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * 订单事件服务实现
 * <p>
 * 支付/超时/取消/退款事件：
 * 校验状态迁移并发送事务消息，本地事务由 {@link com.cloudmall.order.listener.OrderTxMessageListener}
 * 执行，确保状态变更与消息投递的原子性。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderEventService implements IOrderEventService {

    private final OrderMapper orderMapper;
    private final MQProducer mqProducer;

    /** 订单事件 Topic */
    private static final String ORDER_EVENTS_TOPIC = "ORDER_EVENTS";

    /**
     * 支付成功：校验回调金额后发送事务消息，本地事务由监听器执行。
     *
     * @param orderNo 父订单号
     * @param payAmount 第三方回调支付金额
     */
    @Override
    public void handlePaid(String orderNo, BigDecimal payAmount) {
        Asserts.notNull(payAmount, BizErrorEnum.INVALID_PARAM);
        OrderDO order = requireOrder(orderNo);
        BigDecimal convertedAmount = convertThirdPartyAmount(payAmount);
        Asserts.isTrue(
                convertedAmount.compareTo(order.getPayableAmount()) == 0,
                BizErrorEnum.ORDER_PAY_AMOUNT_MISMATCH
        );
        OrderEvent event = OrderEvent.builder()
                                     .orderNo(orderNo)
                                     .userId(order.getUserId())
                                     .build();
        mqProducer.sendInTransaction(ORDER_EVENTS_TOPIC, OrderTagEnum.PAID.getTag(), event, orderNo);
    }

    /**
     * 支付超时：发送事务消息，本地事务由监听器执行。
     *
     * @param orderNo 父订单号
     */
    @Override
    public void handleTimeout(String orderNo) {
        OrderDO order = requireOrder(orderNo);
        OrderEvent event = OrderEvent.builder()
                                     .orderNo(orderNo)
                                     .userId(order.getUserId())
                                     .build();
        mqProducer.sendInTransaction(ORDER_EVENTS_TOPIC, OrderTagEnum.TIMEOUT.getTag(), event, orderNo);
    }

    /**
     * 订单取消：发送事务消息，本地事务由监听器执行。
     *
     * @param orderNo 父订单号
     */
    @Override
    public void handleCancel(String orderNo) {
        OrderDO order = requireOrder(orderNo);
        OrderEvent event = OrderEvent.builder()
                                     .orderNo(orderNo)
                                     .userId(order.getUserId())
                                     .build();
        mqProducer.sendInTransaction(ORDER_EVENTS_TOPIC, OrderTagEnum.CANCELLED.getTag(), event, orderNo);
    }

    /**
     * 全额退款：校验回调金额后发送事务消息，本地事务由监听器执行。
     *
     * @param orderNo 父订单号
     * @param refundId 退款单 ID
     * @param refundAmount 第三方回调退款金额
     */
    @Override
    public void handleFullyRefunded(String orderNo, String refundId, BigDecimal refundAmount) {
        Asserts.notBlank(refundId, BizErrorEnum.INVALID_PARAM);
        Asserts.notNull(refundAmount, BizErrorEnum.INVALID_PARAM);
        OrderDO order = requireOrder(orderNo);
        BigDecimal convertedAmount = convertThirdPartyAmount(refundAmount);
        Asserts.isTrue(
                convertedAmount.compareTo(order.getPayableAmount()) == 0,
                BizErrorEnum.ORDER_REFUND_AMOUNT_MISMATCH
        );
        OrderEvent event = OrderEvent.builder()
                                     .orderNo(orderNo)
                                     .userId(order.getUserId())
                                     .refundId(refundId)
                                     .build();
        mqProducer.sendInTransaction(ORDER_EVENTS_TOPIC, OrderTagEnum.FULLY_REFUNDED.getTag(), event, orderNo);
    }

    /**
     * 按订单号查询父订单。
     *
     * @param orderNo 父订单号
     * @return 父订单
     */
    private OrderDO requireOrder(String orderNo) {
        OrderDO order = Asserts.requireNonNull(orderMapper.selectByOrderNo(orderNo), BizErrorEnum.ORDER_NOT_EXIST);
        return order;
    }

    /**
     * 模拟第三方金额到内部金额单位的转换。 <p>
     * 当前未实现真实单位换算，原样返回入参；接入真实支付渠道后在此补充转换逻辑。
     *
     * @param amount 第三方回调金额
     * @return 转换后的内部金额
     */
    private BigDecimal convertThirdPartyAmount(BigDecimal amount) {
        return amount;
    }
}
