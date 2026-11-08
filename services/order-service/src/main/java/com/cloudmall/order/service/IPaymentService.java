package com.cloudmall.order.service;

import java.math.BigDecimal;

import com.cloudmall.order.model.bo.PaymentOrderSnapshot;

/**
 * 订单支付本地服务。
 * <p>
 * 负责支付场景下的订单查询、归属校验、状态校验与金额单位转换，
 * 远程支付渠道调用与回调编排见 {@link com.cloudmall.order.application.PaymentApp}。
 *
 * @author : Tomatos
 */
public interface IPaymentService {

    /**
     * 查询可支付订单（校验归属与待支付状态）。
     *
     * @param orderNo 父订单号
     * @param userId 当前用户 ID
     * @return 订单支付快照
     */
    PaymentOrderSnapshot getPayableOrder(String orderNo, Long userId);

    /**
     * 查询订单支付快照（校验归属，不限制状态）。
     *
     * @param orderNo 父订单号
     * @param userId 当前用户 ID，为空表示系统内部调用
     * @return 订单支付快照
     */
    PaymentOrderSnapshot getOrder(String orderNo, Long userId);

    /**
     * 分金额转换为元。
     *
     * @param amountTotal 金额（分）
     * @return 金额（元）
     */
    BigDecimal toYuan(Integer amountTotal);
}
