package com.cloudmall.mockpayment.convert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.mockpayment.model.entity.MockPaymentNotifyDO;
import com.cloudmall.mockpayment.model.entity.MockPaymentOrderDO;
import com.cloudmall.mockpayment.model.resp.AmountResp;
import com.cloudmall.mockpayment.model.resp.MockPaymentDetailResp;
import com.cloudmall.mockpayment.model.resp.MockPaymentNotifyResp;
import com.cloudmall.mockpayment.model.resp.PayerResp;
import com.cloudmall.mockpayment.model.resp.TransactionQueryResp;
import com.cloudmall.payment.api.model.bo.WechatPayTransaction;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;
import com.cloudmall.payment.api.model.resp.MockPaymentQueryResp;

/**
 * 模拟支付对象转换器。
 * <p>
 * DO 到微信协议响应、内部 API 响应、模拟控制台响应的字段映射统一收敛于此。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface MockPaymentConverter {

    /**
     * 支付单转微信支付查询响应。
     *
     * @param order 支付单
     * @return 微信支付查询响应
     */
    @Mapping(target = "tradeState", source = "tradeState")
    @Mapping(target = "tradeStateDesc", source = "tradeStateDesc")
    @Mapping(target = "amount", expression = "java(toAmount(order))")
    @Mapping(target = "payer", expression = "java(toPayer(order.getPayerOpenid()))")
    @Mapping(target = "successTime", expression = "java(formatTime(order.getSuccessTime()))")
    TransactionQueryResp toQueryResp(MockPaymentOrderDO order);

    /**
     * 支付单转回调明文交易报文。
     *
     * @param order 支付单
     * @return 交易报文
     */
    @Mapping(target = "successTime", expression = "java(formatTime(order.getSuccessTime()))")
    @Mapping(target = "payer", expression = "java(new WechatPayTransaction.Payer(order.getPayerOpenid()))")
    @Mapping(
            target = "amount",
            expression = "java(new WechatPayTransaction.Amount("
                                 + "order.getAmountTotal(), order.getAmountTotal(), "
                                 + "order.getCurrency(), order.getCurrency()))"
    )
    WechatPayTransaction toTransaction(MockPaymentOrderDO order);

    /**
     * 支付单转内部下单响应。
     *
     * @param order 支付单
     * @param publicBaseUrl 对外基础地址
     * @return 内部下单响应
     */
    @Mapping(
            target = "cashierUrl",
            expression = "java(buildCashierUrl(publicBaseUrl, order.getOutTradeNo()))"
    )
    MockPaymentCreateResp toCreateResp(MockPaymentOrderDO order, String publicBaseUrl);

    /**
     * 支付单转内部查询响应。
     *
     * @param order 支付单
     * @param publicBaseUrl 对外基础地址
     * @return 内部查询响应
     */
    @Mapping(
            target = "cashierUrl",
            expression = "java(buildCashierUrl(publicBaseUrl, order.getOutTradeNo()))"
    )
    MockPaymentQueryResp toQueryRespForInternal(MockPaymentOrderDO order, String publicBaseUrl);

    /**
     * 支付单转模拟控制台详情。
     *
     * @param order 支付单
     * @param publicBaseUrl 对外基础地址
     * @return 支付单详情
     */
    @Mapping(
            target = "cashierUrl",
            expression = "java(buildCashierUrl(publicBaseUrl, order.getOutTradeNo()))"
    )
    MockPaymentDetailResp toDetailResp(MockPaymentOrderDO order, String publicBaseUrl);

    /**
     * 通知记录转控制台响应。
     *
     * @param notify 通知记录
     * @return 通知响应
     */
    MockPaymentNotifyResp toNotifyResp(MockPaymentNotifyDO notify);

    /**
     * 批量转换通知记录。
     *
     * @param notifies 通知记录
     * @return 通知响应
     */
    List<MockPaymentNotifyResp> toNotifyRespList(List<MockPaymentNotifyDO> notifies);

    /**
     * 构造支付者响应。
     *
     * @param openid 支付者标识
     * @return 支付者响应
     */
    default PayerResp toPayer(String openid) {
        if (openid == null) {
            return null;
        }
        return PayerResp.builder()
                        .openid(openid)
                        .build();
    }

    /**
     * 构造订单金额响应。
     *
     * @param order 支付单
     * @return 金额响应
     */
    default AmountResp toAmount(MockPaymentOrderDO order) {
        return AmountResp.builder()
                         .total(order.getAmountTotal())
                         .payerTotal(order.getAmountTotal())
                         .currency(order.getCurrency())
                         .payerCurrency(order.getCurrency())
                         .build();
    }

    /**
     * 格式化 RFC3339 时间。
     *
     * @param time 本地时间
     * @return RFC3339 字符串
     */
    default String formatTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * 构造收银台地址。
     *
     * @param publicBaseUrl 对外基础地址
     * @param outTradeNo 商户订单号
     * @return 收银台地址
     */
    default String buildCashierUrl(String publicBaseUrl, String outTradeNo) {
        return publicBaseUrl + "/mock/cashier/" + outTradeNo;
    }
}
