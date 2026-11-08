package com.cloudmall.order.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.enums.OrderTagEnum;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.order.service.IOrderStateMachineService;
import com.cloudmall.rocketmq.producer.TxMessageHeader;

/**
 * 订单事务消息监听器
 * <p>
 * 处理订单事件的本地事务执行和状态回查。
 * <ul>
 *   <li>executeLocalTransaction：执行订单状态变更（支付/超时/取消/退款）</li>
 *   <li>checkLocalTransaction：回查订单状态判断事务是否成功</li>
 * </ul>
 *
 * @author : Tomatos
 */
@Slf4j
@RequiredArgsConstructor
@RocketMQTransactionListener
public class OrderTxMessageListener implements RocketMQLocalTransactionListener {

    private final OrderMapper orderMapper;
    private final IOrderStateMachineService stateMachine;

    /**
     * 执行本地事务：根据事件类型执行订单状态变更。
     *
     * @param msg 消息
     * @param arg 事务回调参数（固定为 null，业务主键从 header 读取）
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String orderNo = (String) msg.getHeaders().get(TxMessageHeader.EVENT_ID);
        String eventType = (String) msg.getHeaders().get(TxMessageHeader.EVENT_TYPE);
        OrderTagEnum tagEnum = OrderTagEnum.from(eventType);
        try {
            OrderDO order = requireOrder(orderNo);
            switch (tagEnum) {
                case PAID -> stateMachine.transitionParent(
                        order.getId(),
                        ParentOrderStatusEnum.PENDING_PAYMENT,
                        ParentOrderStatusEnum.PAID
                );
                case TIMEOUT -> stateMachine.transitionParent(
                        order.getId(),
                        ParentOrderStatusEnum.PENDING_PAYMENT,
                        ParentOrderStatusEnum.TIMEOUT
                );
                case CANCELLED -> {
                    int updated = orderMapper.markCancelled(order.getId(), ParentOrderStatusEnum.CANCELLED);
                    Asserts.isTrue(updated > 0, BizErrorEnum.INVALID_ORDER_STATUS);
                }
                case FULLY_REFUNDED -> {
                    int refundUpdated = orderMapper.markRefunded(order.getId());
                    Asserts.isTrue(refundUpdated > 0, BizErrorEnum.INVALID_ORDER_STATUS);
                }
                default -> {
                    log.warn("未知的订单事件类型 eventType={}", eventType);
                    return RocketMQLocalTransactionState.ROLLBACK;
                }
            }
            log.info("订单事务消息本地事务执行成功 orderNo={} eventType={}", orderNo, eventType);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("订单事务消息本地事务执行失败 orderNo={} eventType={}", orderNo, eventType, e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 回查本地事务状态：查询订单表判断事务是否成功。
     *
     * @param msg 消息
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String orderNo = (String) msg.getHeaders().get(TxMessageHeader.EVENT_ID);
        String eventType = (String) msg.getHeaders().get(TxMessageHeader.EVENT_TYPE);
        OrderTagEnum tagEnum = OrderTagEnum.from(eventType);
        OrderDO order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.info("事务消息回查：订单不存在 orderNo={}，回滚", orderNo);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        // 根据事件类型判断订单状态是否已变更
        boolean committed = switch (tagEnum) {
            case PAID -> ParentOrderStatusEnum.PAID.getCode().equals(order.getStatus());
            case TIMEOUT -> ParentOrderStatusEnum.TIMEOUT.getCode().equals(order.getStatus());
            case CANCELLED -> ParentOrderStatusEnum.CANCELLED.getCode().equals(order.getStatus());
            case FULLY_REFUNDED -> ParentOrderStatusEnum.REFUNDED.getCode().equals(order.getStatus());
            default -> false;
        };
        if (committed) {
            log.info("事务消息回查：订单状态已变更 orderNo={} eventType={}，提交", orderNo, eventType);
            return RocketMQLocalTransactionState.COMMIT;
        }
        log.info("事务消息回查：订单状态未变更 orderNo={} eventType={}，稍后再查", orderNo, eventType);
        return RocketMQLocalTransactionState.UNKNOWN;
    }

    /**
     * 按订单号查询父订单。
     */
    private OrderDO requireOrder(String orderNo) {
        return Asserts.requireNonNull(orderMapper.selectByOrderNo(orderNo), BizErrorEnum.ORDER_NOT_EXIST);
    }
}
