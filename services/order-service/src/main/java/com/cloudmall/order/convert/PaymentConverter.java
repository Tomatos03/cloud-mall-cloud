package com.cloudmall.order.convert;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.order.model.resp.PaymentCreateResp;
import com.cloudmall.order.model.resp.PaymentStatusResp;
import com.cloudmall.payment.api.enums.TradeStateEnum;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;
import com.cloudmall.payment.api.model.resp.MockPaymentQueryResp;

/**
 * 支付对象转换器。
 * <p>
 * 将模拟支付服务的内部响应转换为订单服务对外支付响应，金额由分转换为元。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface PaymentConverter {

    /**
     * 创建支付单响应转换。
     *
     * @param resp 模拟支付下单响应
     * @param amount 应付金额（元）
     * @return 订单服务支付响应
     */
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "orderNo", source = "resp.outTradeNo")
    PaymentCreateResp toCreateResp(MockPaymentCreateResp resp, BigDecimal amount);

    /**
     * 支付状态响应转换。
     *
     * @param resp 模拟支付查询响应
     * @return 订单服务支付状态响应
     */
    @Mapping(target = "orderNo", source = "outTradeNo")
    @Mapping(target = "paid", expression = "java(isPaid(resp))")
    @Mapping(target = "amount", expression = "java(toYuan(resp.getAmountTotal()))")
    PaymentStatusResp toStatusResp(MockPaymentQueryResp resp);

    /**
     * 判断支付渠道交易状态是否成功。
     *
     * @param resp 模拟支付查询响应
     * @return true 表示支付成功
     */
    default boolean isPaid(MockPaymentQueryResp resp) {
        return TradeStateEnum.SUCCESS.getCode().equals(resp.getTradeState());
    }

    /**
     * 分金额转换为元。
     *
     * @param amountTotal 金额（分）
     * @return 金额（元）
     */
    default BigDecimal toYuan(Integer amountTotal) {
        int total = amountTotal == null ? 0 : amountTotal;
        return BigDecimal.valueOf(total).movePointLeft(2);
    }
}
