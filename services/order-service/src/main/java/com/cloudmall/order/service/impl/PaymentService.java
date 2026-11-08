package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.model.bo.PaymentOrderSnapshot;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.order.service.IPaymentService;

/**
 * 订单支付本地服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final OrderMapper orderMapper;

    /**
     * 查询可支付订单。
     *
     * @param orderNo 父订单号
     * @param userId 当前用户 ID
     * @return 订单支付快照
     */
    @Override
    public PaymentOrderSnapshot getPayableOrder(String orderNo, Long userId) {
        PaymentOrderSnapshot snapshot = getOrder(orderNo, userId);
        Asserts.isTrue(snapshot.isPendingPayment(), BizErrorEnum.PAYMENT_ORDER_NOT_PAYABLE);
        return snapshot;
    }

    /**
     * 查询订单支付快照。
     *
     * @param orderNo 父订单号
     * @param userId 当前用户 ID，为空表示系统内部调用
     * @return 订单支付快照
     */
    @Override
    public PaymentOrderSnapshot getOrder(String orderNo, Long userId) {
        OrderDO order = Asserts.requireNonNull(
                orderMapper.selectByOrderNo(orderNo),
                BizErrorEnum.ORDER_NOT_EXIST
        );
        if (userId != null) {
            Asserts.isTrue(Objects.equals(order.getUserId(), userId), BizErrorEnum.ORDER_NOT_BELONG_TO_USER);
        }
        return new PaymentOrderSnapshot(
                order.getOrderNo(),
                order.getUserId(),
                order.getPayableAmount(),
                order.getStatus()
        );
    }

    /**
     * 分金额转换为元。
     *
     * @param amountTotal 金额（分）
     * @return 金额（元）
     */
    @Override
    public BigDecimal toYuan(Integer amountTotal) {
        Asserts.notNull(amountTotal, BizErrorEnum.MOCK_PAYMENT_AMOUNT_INVALID);
        return BigDecimal.valueOf(amountTotal).movePointLeft(2);
    }
}
