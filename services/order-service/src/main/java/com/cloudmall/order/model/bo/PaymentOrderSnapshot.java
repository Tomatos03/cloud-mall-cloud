package com.cloudmall.order.model.bo;

import java.math.BigDecimal;

import com.cloudmall.order.enums.ParentOrderStatusEnum;

/**
 * 支付场景订单快照。
 * <p>
 * Service 内部将订单实体裁剪为支付所需字段，避免 DO 进入 Application 层。
 *
 * @param orderNo 父订单号
 * @param userId 下单用户 ID
 * @param payableAmount 应付金额（元）
 * @param status 订单状态
 * @author : Tomatos
 */
public record PaymentOrderSnapshot(
    String orderNo,
    Long userId,
    BigDecimal payableAmount,
    ParentOrderStatusEnum status
) {

    /**
     * 是否已支付。
     *
     * @return true 表示已支付
     */
    public boolean isPaid() {
        return status == ParentOrderStatusEnum.PAID;
    }

    /**
     * 是否待支付。
     *
     * @return true 表示待支付
     */
    public boolean isPendingPayment() {
        return status == ParentOrderStatusEnum.PENDING_PAYMENT;
    }
}
